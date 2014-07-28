/**
 * 
 */
package uk.bl.wa.shine;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;
import org.apache.solr.common.SolrDocument;
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

	private SolrQuery buildInitialParameters(Query query) {
		SolrQuery parameters = new SolrQuery();

		ORDER orderSolr = ORDER.asc;

		if (StringUtils.equalsIgnoreCase(query.order, "desc")) {
			orderSolr = ORDER.desc;
		}
		if (StringUtils.isEmpty(query.sort)) {
			query.sort = "crawl_date";
		}
		
		parameters.setSort(query.sort, orderSolr);

		Integer start = ((query.page - 1) * perPage);
		if (start < 0) {
			start = 0;
		}

		parameters.set("start", start);
		// Sorts:
		// parameters.setSort("sentiment_score", ORDER.asc);
		Logger.info("params: " + parameters);
		
		return parameters;
	}

	public Query search(Query query) throws SolrServerException {
		return this.search(query, perPage);
	}

	public Query export(Query query) throws SolrServerException {
		Query q = this.search(query, 0);
		int total = (int)q.res.getResults().getNumFound();
		Logger.info("exporting total: " + total);
		return this.search(query, total);
	}
	
	public Query search(Query query, int rows) throws SolrServerException {
		return this.search(query, buildInitialParameters(query), rows);
	}
	
	public Query advancedSearch(Query query) throws SolrServerException {
		return this.advancedSearch(query, buildInitialParameters(query));
	}
	
	public Query browse(Query query) throws SolrServerException {
		return this.browse(query, buildInitialParameters(query));
	}

	public Query graph(Query query) throws SolrServerException {
		return this.graph(query, buildInitialParameters(query));
	}

//	private Query search(Query query, SolrQuery solrParameters) throws SolrServerException {
//		return this.search(query, solrParameters, perPage);
//	}
	
	// usually for faceted search
	private Query search(Query query, SolrQuery solrParameters, int rows) throws SolrServerException {
		
		Map<String, List<String>> parameters = query.getParameters();
		
	    List<String> actionParameters = parameters.get("action");

	    String selectedFacet = null;
	    String removeFacet = null;

	    query.facetValues = facetService.getSelected();
	    
	    // check incoming parameter list
	    Logger.info("pre-adding>>>> " + query.facetValues);
	    
	    if (actionParameters != null) {
		    String action = actionParameters.get(0);
			if (action.equals("add-facet")) {
			    selectedFacet = parameters.get("selected.facet").get(0);
			    FacetValue selectedFacetValue = getFacetValueByName(selectedFacet);
			    
			    // TODO: if it doesn't already haveit in there
			    if (!query.facets.contains(selectedFacet)) {
				    // from url parameters
				    query.facets.add(selectedFacet);
				    // for filtering
				    query.facetValues.put(selectedFacetValue.getName(), selectedFacetValue);
				    // for dropdown list
				    facetService.getOptionals().remove(selectedFacet);
			    }
			    
			} else if (action.equals("remove-facet")) {
			    removeFacet = parameters.get("remove.facet").get(0);
			    FacetValue selectedFacetValue = getFacetValueByName(removeFacet);
			    
			    // from url parameters
				query.facets.remove(removeFacet);
			    // for filtering
			    query.facetValues.remove(removeFacet);
			    // for dropdown list
			    facetService.getOptionals().put(selectedFacetValue.getName(), selectedFacetValue);
			    
			    Logger.info("removing>>>> " + selectedFacetValue.getName());
			} else if (action.equals("search")) {
				//parameters.setParam("wt", "json");
				// get the defaults
				// facets that come from url parameters
			    String[] facets = query.facets.toArray(new String[query.facets.size()]);
			    
			    Logger.info("searching....");
			    
				for (String facet : facets) {
//					if (StringUtils.isNotEmpty(removeFacet) && !removeFacet.equals(facet)) {
						Logger.info("facet >>>>>>>>>>>>>>> " + facet);
					    FacetValue selectedFacetValue = getFacetValueByName(facet);
						Logger.info("selectedFacetValue >>>>>>>>>>>>>>> " + selectedFacetValue.getValue());
					    query.facets.add(facet);
					    query.facetValues.put(selectedFacetValue.getName(), selectedFacetValue);
						Logger.info("query.facetValues >>> " + query.facetValues);
//					}
				}
			}
		    Logger.info("post: query.facets: " + query.facets);
		    Logger.info("post: query.facetValues: " + query.facetValues);
	    }
		
		// TODO: what if you need all results for exporting?
		solrParameters.setRows(rows);
		Logger.info("ROWS>>>>" + rows);
		// get me 0 rows first and re-get with total
		return doSearch(query, solrParameters);
	}


	private Query advancedSearch(Query query, SolrQuery parameters) throws SolrServerException {
		// facets available on the advanced search fields
		Map<String, FacetValue> facetValues = new HashMap<String, FacetValue>();
		// build up facetValues with parameters
		Logger.info("advancedSearch parameters: " + query.filters + " - " + parameters);
		parameters.setRows(perPage);
		query.facetValues = facetValues;
		return doSearch(query, parameters);
	}

	private Query browse(Query query, SolrQuery parameters) throws SolrServerException {
		// facets available on the advanced search fields
		Map<String, FacetValue> facetValues = new HashMap<String, FacetValue>();
		FacetValue collectionFacetValue = new FacetValue("collection", "Collection");
		FacetValue collectionsFacetValue = new FacetValue("collections", "Collections");
		facetValues.put(collectionFacetValue.getName(), collectionFacetValue);
		facetValues.put(collectionsFacetValue.getName(), collectionsFacetValue);
		// build up the facets and add to map to pass on 
		Logger.info("browse facetValues: " + facetValues);
		parameters.setRows(perPage);
		query.facetValues = facetValues;
		return doSearch(query, parameters);
	}

	private Query graph(Query query, SolrQuery solrParameters) throws SolrServerException {

		Map<String, FacetValue> facetValues = new HashMap<String, FacetValue>();
		FacetValue crawlDateFacetValue = new FacetValue("crawl_year", "Crawl Year");
		facetValues.put(crawlDateFacetValue.getName(), crawlDateFacetValue);
		query.facetValues = facetValues;
	    // select?q=*:*&facet=true&facet.date=crawl_date&facet.date.gap=%2B1YEAR&facet.date.start=1994-01-01T00:00:00.00Z&facet.date.end=NOW%2B1YEAR
		// select?sort=content_type_norm+asc&start=0&rows=10&q=nhs&facet.mincount=1&fq=crawl_date%3A%5B2005-05-14T00%3A00%3A00Z+TO+2014-05-14T00%3A00%3A00Z%5D
		
		//select?
		//facet.range=crawl_date
		//&f.crawl_date.facet.range.start=2000-01-01T12%3A00%3A00.000Z
		//&f.crawl_date.facet.range.end=2005-05-15T12%3A17%3A56.632Z
		//&f.crawl_date.facet.range.gap=%2B1YEAR
		//&facet=true
		//&facet.sort=index
		//&q=*%3A*

		//parameters.setParam("wt", "json");
		// get the defaults
		// facets that come from url parameters

		if (StringUtils.isEmpty(query.yearStart)) {
			query.yearStart = "1980";
		}

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, Integer.parseInt(query.yearStart));
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date start = cal.getTime();
		
		// for +1 year
		cal.setTime(new Date());
		if (StringUtils.isEmpty(query.yearEnd)) {
			query.yearEnd = String.valueOf(cal.get(Calendar.YEAR));
		}
		cal.set(Calendar.YEAR, Integer.parseInt(query.yearEnd));
		//cal.add(Calendar.YEAR, 1); // to get previous year add -1
		Date end = cal.getTime();
		Logger.info("start date: " + start);
		Logger.info("end date: " + end);
		solrParameters.addDateRangeFacet("crawl_date", start, end, "+1YEAR");
		solrParameters.setFacetSort("index");
		Logger.info("start date: " + query.yearStart);
		Logger.info("start end: " + query.yearEnd);
		return doSearch(query, solrParameters);
	}

	// for advanced search using own facets
	private Query doSearch(Query query, SolrQuery parameters) throws SolrServerException {

		try {
			// add everything to parameters for solr
			if (parameters == null)
				parameters = new SolrQuery();
			// The query:
			// ?start=0&sort=content_type_norm+asc&q=wikipedia+crawl_date:[2009-06-01T00%3A00%3A00Z+TO+2011-06-01T00%3A00%3A00Z]&facet.field={!ex%3Dcrawl_year}crawl_year&facet.field={!ex%3Dpublic_suffix}public_suffix&facet=true&facet.mincount=1&rows=10
			if (StringUtils.isEmpty(query.query)) {
				query.query = "*:*";
			}
			parameters.add("q", query.query);
	
			Map<String, FacetValue> facetValues = query.facetValues;
			
			// should get updated list of added/removed facet values
			Logger.info("doSearch:facetValues: " + facetValues);
			if (facetValues != null) {
				for (String key : facetValues.keySet()) {
					FacetValue facetValue = facetValues.get(key);
					if (facetValue != null && StringUtils.isNotEmpty(facetValue.getValue())) {
						parameters.addFacetField("{!ex=" + facetValue.getName() + "}"
								+ facetValue.getName());
					}
				}
			}
			
	//		for(String facet : query.facets) {
	//			parameters.addFacetField("{!ex=" + facet + "}" + facet);
	//		}
			
			Map<String, List<String>> params = query.filters;
	
			parameters.setFacetMinCount(1);
			List<String> fq = new ArrayList<String>();
			for (String param : params.keySet()) {
				String field = param;
				if (!param.equals("facet.sort")) {
					parameters.setFacetSort("index");
				}
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
	
			try {
				processExcluded(parameters, query.excluded);
				processWebsiteTitle(parameters, query.websiteTitle);
				if (StringUtils.isNotEmpty(query.pageTitle)) {
					parameters.addFilterQuery("title:" + query.pageTitle);
				}
				if (StringUtils.isNotEmpty(query.author)) {
					parameters.add("author", query.author);
				}
				if (StringUtils.isNotEmpty(query.url)) {
					parameters.add("url", query.url);
				}
				if (StringUtils.isNotEmpty(query.fileFormat)) {
					parameters.add("content_type", query.fileFormat);
				}
				processDateRange(parameters, query.dateStart, query.dateEnd);
				processProximity(parameters, query.proximity);
				
//				processExcluded(parameters, query.excluded);
				processHostDomainPublicSuffix(parameters, query.hostDomainPublicSuffix);
//				processUrlHostDomainPublicSuffix(parameters, query.urlHostDomainPublicSuffix);
			} catch (ParseException e) {
				throw new SolrServerException(e);
			}
	
			Logger.info("Query: " + parameters.toString());
			// Perform the query:
			QueryResponse res = solr.query(parameters);
			Logger.info("QTime: " + res.getQTime());
			Logger.info("Response Header: " + res.getResponseHeader());
			
			
			
	//		List<FacetField> fields = res.getFacetFields();
	//		for (FacetField field : fields) {
	//			FacetValue fv = facetService.getAll().get(field.getName());
	//			Logger.info("fv: " + fv.getName() + " - " + fv.getValue());
	//		}
			query.res = res;
			query.processQueryResponse();
			
			// might take a long time
			List<SolrDocument> docs = query.res.getResults();
			
			if (!query.res.getResults().isEmpty()) {
				for (Iterator<SolrDocument> iterator = docs.iterator(); iterator.hasNext(); ) {
					SolrDocument doc = iterator.next();
					if (query.getExclusions().contains(String.valueOf(doc.getFirstValue("id_long")))) {
						Logger.info("matched: " + String.valueOf(doc.getFirstValue("id_long")) + " - " + doc.getFirstValue("title"));
				        iterator.remove();
					}
				}
			}
		} catch(ParseException e) {
			throw new SolrServerException(e);
		}
		return query;
	}

	private void processDateRange(SolrQuery parameters, String dateStart,
			String dateEnd) throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
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

	private void processWebsiteTitle(SolrQuery parameters, String websiteTitle) {
//		Website Title = 'title' where 'url_type'='SLASHPAGE' (i.e. not all pages)
		if (StringUtils.isNotEmpty(websiteTitle)) {
			parameters.addFilterQuery("title:" + websiteTitle);
			parameters.addFilterQuery("url_type:SLASHPAGE");
		}
	}

	private void processExcluded(SolrQuery parameters, String excluded) {
		if (StringUtils.isNotEmpty(excluded)) {
			String[] exclusions = excluded.split(",");
			for (String exclude : exclusions) {
				parameters.addFilterQuery("-" + exclude.trim());
			}
		}
	}

	private void processHostDomainPublicSuffix(SolrQuery parameters, String hostDomainPublicSuffix) {
//		Host = 'host' or 'domain' depending on Solr index schema version.
		if (StringUtils.isNotEmpty(hostDomainPublicSuffix)) {
			parameters.add("domain", hostDomainPublicSuffix);
		}
	}

//	private void processUrlHostDomainPublicSuffix(SolrQuery parameters, String urlHostDomainPublicSuffix) {
//		
//	}
//	
//	private void processFacetField(SolrQuery parameters, String facetField) {
//		// facet.in.public_suffix="co.uk"
//	}

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

	public Map<String, FacetValue> getFacetValues() {
		return facetService.getAll();
	}

	public FacetValue getFacetValueByName(String facetName) {
    	return facetService.getAll().get(facetName);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

}
