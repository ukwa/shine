/**
 * 
 */
package uk.bl.wa.shine;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;
import org.apache.solr.common.params.ModifiableSolrParams;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.Logger;
import play.libs.Json;
import uk.bl.wa.shine.model.FacetValue;
import uk.bl.wa.shine.service.FacetService;
import uk.bl.wa.shine.service.FacetServiceImpl;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 * 
 */
public class Shine extends Solr {

	private int perPage;

	private FacetService facetService = null;

	public Shine(play.Configuration config) {
		super(config);
		this.facetService = new FacetServiceImpl(config);
		this.perPage = config.getInt("per_page");
	}

	public QueryResponse search(Query query) throws SolrServerException {
		return this.search(query, null);
	}

	private SolrQuery buildInitialParameters(int pageNo, String sort, String order) {
		ORDER orderSolr = ORDER.asc;

		if (StringUtils.equalsIgnoreCase(order, "desc")) {
			orderSolr = ORDER.desc;
		}
		if (StringUtils.isEmpty(sort)) {
			sort = "crawl_date";
		}

		Logger.info("per_page: " + perPage);

		Integer start = ((pageNo - 1) * perPage);
		if (start < 0) {
			start = 0;
		}

		SolrQuery parameters = new SolrQuery();
		parameters.set("start", start);
		// Sorts:
		parameters.setSort(sort, orderSolr);
		// parameters.setSort("sentiment_score", ORDER.asc);
		Logger.info("params: " + parameters);
		
		return parameters;
	}
	
	public QueryResponse search(Query query, int pageNo, String sort, String order) throws SolrServerException {
		QueryResponse res = this.search(query, buildInitialParameters(pageNo, sort, order));
		return res;
	}
	
	public QueryResponse advancedSearch(Query query, int pageNo, String sort, String order) throws SolrServerException {
		return this.advancedSearch(query, buildInitialParameters(pageNo, sort, order));
	}
	
	public QueryResponse browse(Query query, int pageNo, String sort, String order) throws SolrServerException {
		return this.browse(query, buildInitialParameters(pageNo, sort, order));
	}
	
	// usually for faceted search
	private QueryResponse search(Query query, SolrQuery parameters) throws SolrServerException {
		// selected facets
		parameters.setRows(perPage);
		return search(query, parameters, facetService.getSelected());
	}


	private QueryResponse advancedSearch(Query query, SolrQuery parameters) throws SolrServerException {
		// facets available on the advanced search fields
		Map<String, FacetValue> facetValues = new HashMap<String, FacetValue>();
		FacetValue collectionsFacetValue = new FacetValue("collections", "Collections");
		facetValues.put(collectionsFacetValue.getName(), collectionsFacetValue);
		// build up the facets and add to map to pass on 
		parameters.setRows(perPage);
		return search(query, parameters, facetValues);
	}

	private QueryResponse browse(Query query, SolrQuery parameters) throws SolrServerException {
		// facets available on the advanced search fields
		Map<String, FacetValue> facetValues = new HashMap<String, FacetValue>();
		FacetValue collectionsFacetValue = new FacetValue("collection", "Collection");
		facetValues.put(collectionsFacetValue.getName(), collectionsFacetValue);
		// build up the facets and add to map to pass on 
		Logger.info("browse facetValues: " + facetValues);
		parameters.setRows(0);
		return search(query, parameters, facetValues);
	}

	// for advanced search using own facets
	private QueryResponse search(Query query, SolrQuery parameters, Map<String, FacetValue> facetValues)
			throws SolrServerException {

		// add everything to parameters for solr
		if (parameters == null)
			parameters = new SolrQuery();
		// The query:
		// ?start=0&sort=content_type_norm+asc&q=wikipedia+crawl_date:[2009-06-01T00%3A00%3A00Z+TO+2011-06-01T00%3A00%3A00Z]&facet.field={!ex%3Dcrawl_year}crawl_year&facet.field={!ex%3Dpublic_suffix}public_suffix&facet=true&facet.mincount=1&rows=10
		parameters.add("q", query.query);

		// should get updated list of added/removed facet values
		Logger.info("facetValues: " + facetValues);
		if (facetValues != null) {
			for (String key : facetValues.keySet()) {
				FacetValue facetValue = facetValues.get(key);
				if (facetValue != null)
					parameters.addFacetField("{!ex=" + facetValue.getName() + "}"
							+ facetValue.getName());
			}
		}
		
		Map<String, List<String>> params = query.filters;

		parameters.setFacetMinCount(1);
		List<String> fq = new ArrayList<String>();
		for (String param : params.keySet()) {
			String field = param;
			if (param.equals("facet.sort")) {
				// there's only one sort
				parameters.setFacetSort(params.get(param).get(0));
			}
			// Excluded tags are ANDed together:
			else if (param.startsWith("-")) {
				field = field.replaceFirst("-", "");
				for (String val : params.get(param)) {
					fq.add("{!tag=" + field + "}" + param + ":" + val); // TODO
																		// Escape
																		// correctly?
				}
			} else {
				// Included tags are ORed together:
				String filter = "{!tag=" + field + "}" + param + ":(";
				int counter = 0;
				for (String val : params.get(param)) {
					if (counter > 0)
						filter += " OR ";
					filter += "" + val + ""; // TODO Escape correctly?
					counter++;

				}
				filter += ")";
				fq.add(filter);
			}
		}
		if (fq.size() > 0) {
			parameters.setFilterQueries(fq.toArray(new String[fq.size()]));
		}

		Logger.info("Pre Query: " + parameters.toString());

		try {
			processDateRange(parameters, query.dateStart, query.dateEnd);
			processProximity(parameters, query.proximity);
		} catch (ParseException e) {
			throw new SolrServerException(e);
		}

		Logger.info("Query: " + parameters.toString());
		// Perform the query:
		QueryResponse res = solr.query(parameters);
		Logger.info("QTime: " + res.getQTime());
		Logger.info("Response Header: " + res.getResponseHeader());
		Logger.info("facet fields: " + res.getFacetFields());
		return res;
	}

	private void processDateRange(SolrQuery parameters, String dateStart,
			String dateEnd) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		Logger.info("dateEnd: " + dateEnd);
		Date dateObjStart = null;
		Date dateObjEnd = null;
		if (StringUtils.isNotEmpty(dateStart)) {
			dateObjStart = formatter.parse(dateStart);
			Logger.info("dateStart: " + dateStart);
		}
		if (StringUtils.isNotEmpty(dateEnd)) {
			dateObjEnd = formatter.parse(dateEnd);
			Logger.info("dateStart: " + dateEnd);
		}
		if (dateObjStart != null && dateObjEnd != null) {
			formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
			parameters.addFilterQuery("crawl_date:["
					+ formatter.format(dateObjStart) + " TO "
					+ formatter.format(dateObjEnd) + "]");
		}
	}

	private void processProximity(SolrQuery parameters, Proximity proximity) {
		StringBuilder builder = null;
		if (proximity != null && (StringUtils.isNotEmpty(proximity.getPhrase1())
				&& StringUtils.isNotEmpty(proximity.getPhrase2()))) {
			builder = new StringBuilder();
			if (StringUtils.isNotEmpty(proximity.getProximity())) {
				// If there is a PROXIMITY, then this should implement the
				// appropriate query syntax.
				builder.append("\"").append(proximity.getPhrase1()).append(" ")
						.append(proximity.getPhrase2()).append("\"")
						.append("~").append(proximity.getProximity());
			} else {
				// If there is no PROXIMITY, then this is just an AND query.
				builder.append(proximity.getPhrase1()).append(" AND ")
						.append(proximity.getPhrase2());
			}
			Logger.info("builder parameters: " + builder.toString());
			parameters.setQuery(builder.toString());
		}
	}
	
	private void processFacetField(SolrQuery parameters, String facetField) {
		// facet.in.public_suffix="co.uk"
	}

	public JsonNode suggestTitle(String name) throws SolrServerException {
		return suggest(name, "/suggestTitle"); 
	}

	public JsonNode suggestUrl(String name) throws SolrServerException {
		return suggest(name, "/suggestUrl"); 
	}

	public JsonNode suggestFileFormat(String name) throws SolrServerException {
		return suggest(name, "/suggestFileFormat"); 
	}

	public JsonNode suggestHost(String name) throws SolrServerException {
		return suggest(name, "/suggestHost"); 
	}

	public JsonNode suggestDomain(String name) throws SolrServerException {
		return suggest(name, "/suggestDomain"); 
	}

	public JsonNode suggestPublicSuffix(String name) throws SolrServerException {
		return suggest(name, "/suggestPublicSuffix"); 
	}

	public JsonNode suggestLinksHosts(String name) throws SolrServerException {
		return suggest(name, "/suggestLinksHosts"); 
	}

	public JsonNode suggestLinksDomains(String name) throws SolrServerException {
		return suggest(name, "/suggestLinksDomains"); 
	}

	public JsonNode suggestLinksPublicSuffixes(String name) throws SolrServerException {
		return suggest(name, "/suggestLinksPublicSuffixes"); 
	}

	public JsonNode suggestAuthor(String name) throws SolrServerException {
		return suggest(name, "/suggestAuthor"); 
	}

	public JsonNode suggestCollection(String name) throws SolrServerException {
		return suggest(name, "/suggestCollection"); 
	}

	public JsonNode suggestCollections(String name) throws SolrServerException {
		return suggest(name, "/suggestCollections"); 
	}

	private JsonNode suggest(String name, String suggestPath) throws SolrServerException {

		JsonNode jsonData = null;
		List<ObjectNode> result = new ArrayList<ObjectNode>();
		JsonNodeFactory nodeFactory = new JsonNodeFactory(false);

		try {
			ModifiableSolrParams params = new ModifiableSolrParams();
			
			params.set("qt", suggestPath);
			params.set("q", name);
			params.set("wt", "json");
			QueryResponse response = solr.query(params);
			SpellCheckResponse spellCheckResponse = response
					.getSpellCheckResponse();

			Logger.info("spellCheckResponse: " + spellCheckResponse);

			List<Suggestion> suggestions = spellCheckResponse.getSuggestions();

			if (suggestions != null && suggestions.size() > 0) {
				for (Suggestion suggestion : suggestions) {
					List<String> alternatives = suggestion.getAlternatives();
					if (alternatives != null && alternatives.size() > 0) {
						for (String alternative : alternatives) {
							ObjectNode child = nodeFactory.objectNode();
							child.put("name", alternative);
							result.add(child);
						}
					}
				}
			}
			jsonData = Json.toJson(result);
		} catch (Exception e) {
			// throw new SolrServerException("Suggestions not found: " + e);
			ObjectNode testChild = nodeFactory.objectNode();
			testChild.put("title",
					"Suggestions server isn't working at present");
			result.add(testChild);
			Logger.info("result: " + result);
			jsonData = Json.toJson(result);
		}
		return jsonData;
	}
	
	public Map<String, FacetValue> getSelectedFacets() {
		return this.facetService.getSelected();
	}

	public Map<String, FacetValue> getOptionalFacets() {
		return this.facetService.getOptionals();
	}

	public void addFacet(String facetName) {
		facetService.add(facetName);
	}

	public void removeFacet(String facetName) {
		facetService.remove(facetName);
	}

	public void resetFacets() {
		facetService.reset();
	}

	// private String temp( String query ) throws SolrServerException {
	// QueryResponse res = this.search(query, null, 0, null, null);
	// res.getFacetFields().get(0).getValues().get(0).getName();
	// res.getResults().get(0).getFirstValue("title");
	// res.getResults().getNumFound();
	// return null;
	// }

	public int getPerPage() {
		return perPage;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

}
