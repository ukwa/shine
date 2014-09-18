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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;
import org.apache.solr.common.params.FacetParams;
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
	private String shards;
	private FacetService facetService = null;

	public Shine(play.Configuration config) {
		super(config);
		this.facetService = new FacetServiceImpl(config);
		this.perPage = config.getInt("per_page");
		this.shards = config.getString("shards");
	}

	private SolrQuery buildInitialParameters(Query query) {
		SolrQuery solrParameters = new SolrQuery();

		ORDER orderSolr = ORDER.asc;

		if (StringUtils.equalsIgnoreCase(query.order, "desc")) {
			orderSolr = ORDER.desc;
		}
		if (StringUtils.isEmpty(query.sort)) {
			query.sort = "crawl_date";
		}
		
		solrParameters.setSort(query.sort, orderSolr);

		Integer start = ((query.page - 1) * perPage);
		if (start < 0) {
			start = 0;
		}

		solrParameters.set("start", start);
		// may make this configurable
		solrParameters.setFacetLimit(10);
		
		solrParameters.setParam(FacetParams.FACET_METHOD, FacetParams.FACET_METHOD_enum);
		solrParameters.setParam(FacetParams.FACET_ENUM_CACHE_MINDF, "25");
		
		// add shard is mode set to long
		if (StringUtils.isNotEmpty(query.mode) && StringUtils.equalsIgnoreCase(query.mode, "full") && StringUtils.isNotEmpty(shards)) {
			Logger.info("FULL MODE");
			solrParameters.setParam("shards", shards);
		}
		
		Logger.info("facet methods set");
		// Sorts:
		// parameters.setSort("sentiment_score", ORDER.asc);
		Logger.info("params: " + solrParameters);
		
		return solrParameters;
	}

	public Query search(Query query) throws SolrServerException {
		return this.search(query, perPage);
	}

	public Query search(Query query, int rows) throws SolrServerException {
		return this.search(query, buildInitialParameters(query), rows);
	}
	
	public Query browse(Query query) throws SolrServerException {
		return this.browse(query, buildInitialParameters(query));
	}

	public Query graph(Query query) throws SolrServerException {
		return this.graph(query, buildInitialParameters(query));
	}
	
	public Query export(Query query) throws SolrServerException {
		Query q = this.search(query, 0);
		int total = (int)q.res.getResults().getNumFound();
		Logger.info("exporting total: " + total);
		return this.search(query, total);
	}

	private Query search(Query query, SolrQuery solrParameters, int rows) throws SolrServerException {
		
	    solrParameters.setHighlight(true).setHighlightSnippets(5); //set other params as needed
	    
	    solrParameters.setHighlightSimplePre("<em>");
	    solrParameters.setHighlightSimplePost("</em>");
	    
	    solrParameters.addHighlightField("content,title");
//	    solrParameters.setParam("hl.fl", "*");
//	    solrParameters.setHighlightRequireFieldMatch(Boolean.TRUE);

		solrParameters.setRows(rows);
//		&hl=true
//		&hl.fl=*
//		&hl.simple.pre=%3Cem%3E
//		&hl.simple.post=%3C/em%3E
//		&hl.snippets=5
		
//		&hl=true
//		&hl.snippets=5
//		&hl.simple.pre=%3Cem%3E
//		&hl.simple.post=%3C%2Fem%3E
//		&hl.fl=*
//		&hl.requireFieldMatch=true
		
//		if (parameters.get("facet.sort") == null) {
//			Logger.info("facet.sort: " + parameters.get("facet.sort"));
//			solrParameters.setFacetSort("index");
//			Logger.info("set to index");
//		}
		
		Logger.info("solrParameters: " + solrParameters);

		return doSearch(query, solrParameters);
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
		solrParameters.setFacetSort(FacetParams.FACET_SORT_INDEX);
		return doSearch(query, solrParameters);
	}

	// for advanced search using own facets
	private Query doSearch(Query query, SolrQuery solrParameters) throws SolrServerException {

		try {
			// add everything to parameters for solr
			if (solrParameters == null) {
				solrParameters = new SolrQuery();
			}
			
			// The query:
			String q = query.query;

		    query.facetValues = facetService.getSelected();
		    
			Map<String, List<String>> parameters = query.getParameters();
		    List<String> actionParameters = parameters.get("action");
		    List<String> removeFacets = parameters.get("removeFacet");
		    
		    // check incoming parameter list
		    Logger.info("actionParameters: " + actionParameters);
		    Logger.info("parameters: " + parameters);
		    
		    if (actionParameters != null) {
		    	
			    String action = actionParameters.get(0);
			    
		    	// got through all that state removeFacet=[facet1, facet2, etc] and remove from list
			    if (removeFacets != null) {
				    for (String removeFacet : removeFacets) {
					    FacetValue selectedFacetValue = getFacetValueByName(removeFacet);
					    // from url parameters
						query.facets.remove(removeFacet);
					    // for filtering
					    query.facetValues.remove(removeFacet);
					    // for dropdown list
					    facetService.getOptionals().put(selectedFacetValue.getName(), selectedFacetValue);
					    Logger.info("removing>>>> " + selectedFacetValue.getName());
				    }
			    }
		    	
				if (action.equals("add-facet")) {
				    String addFacet = parameters.get("addFacet").get(0);
				    
				    FacetValue addFacetValue = getFacetValueByName(addFacet);
				    
				    // TODO: if it doesn't already haveit in there
				    if (!query.facets.contains(addFacet)) {
					    // from url parameters
					    query.facets.add(addFacet);
					    // for filtering
					    query.facetValues.put(addFacetValue.getName(), addFacetValue);
					    // for dropdown list
					    facetService.getOptionals().remove(addFacet);
				    }
				} else if (action.equals("search")) {
					//parameters.setParam("wt", "json");
					// get the defaults
					// facets that come from url parameters
				    String[] facets = query.facets.toArray(new String[query.facets.size()]);
				    
					for (String facet : facets) {
					    FacetValue selectedFacetValue = getFacetValueByName(facet);
					    if (selectedFacetValue != null) {
						    query.facets.add(facet);
						    query.facetValues.put(selectedFacetValue.getName(), selectedFacetValue);
					    }
					}
					// &q=wikipedia AND -id:"20080514125602/6B+cyN12vEfEOYgIzZDdw==" AND -id:"20100601200405/wTwHWZVx%2BiTLVo3g9ULPnA=="
					StringBuilder selected = new StringBuilder();
					for (String value : query.getExcludes()) {
						String[] values = value.split(";;;");
						String id = values[0].trim();
						selected.append("AND -id:").append("\"").append(id).append("\"").append(" ");
					}
					Logger.info("excluded: " + selected.toString().trim());
					
					// &q=wikipedia AND -id:"20080514125602/6B+cyN12vEfEOYgIzZDdw==" AND -id:"20100601200405/wTwHWZVx%2BiTLVo3g9ULPnA=="
//					StringBuilder selected = new StringBuilder();
					for (String value : query.getExcludeHosts()) {
						selected.append("AND -host:").append("\"").append(value).append("\"").append(" ");
					}
					Logger.info("excludeHost: " + selected.toString().trim());
					
					if (StringUtils.isNotEmpty(selected.toString())) {
						q = q + " " + selected.toString().trim();
					}
				}
		    }

			solrParameters.add("q", q);
			
			Map<String, FacetValue> facetValues = query.facetValues;
			
			// should get updated list of added/removed facet values
			Logger.info("doSearch:facetValues: " + facetValues);
			if (facetValues != null) {
				for (String key : facetValues.keySet()) {
					FacetValue facetValue = facetValues.get(key);
					if (facetValue != null && StringUtils.isNotEmpty(facetValue.getValue())) {
						solrParameters.addFacetField("{!ex=" + facetValue.getName() + "}"
								+ facetValue.getName());
					}
				}
			}
			
	//		for(String facet : query.facets) {
	//			parameters.addFacetField("{!ex=" + facet + "}" + facet);
	//		}
			
			solrParameters.setFacetMinCount(1);

			// give it a new list to add to
			List<String> fq = processFilterQueries(solrParameters, query.filters, new ArrayList<String>());
			
			try {
				processExcluded(solrParameters, query.excluded);
				
				Logger.info("after... " + solrParameters.toString());

				processWebsiteTitle(solrParameters, query.websiteTitle);
				if (StringUtils.isNotEmpty(query.pageTitle)) {
					solrParameters.addFilterQuery("title:" + query.pageTitle);
				}
				if (StringUtils.isNotEmpty(query.author)) {
					solrParameters.add("author", query.author);
				}
				if (StringUtils.isNotEmpty(query.url)) {
					solrParameters.add("url", query.url);
				}
				if (StringUtils.isNotEmpty(query.fileFormat)) {
					solrParameters.add("content_type", query.fileFormat);
				}
				processDateRange(solrParameters, query.dateStart, query.dateEnd);
				processProximity(solrParameters, query.proximity);
				
				if (StringUtils.isNotEmpty(query.hostDomainPublicSuffix)) {
//					{!tag=host}host:("theregister.co.uk")
//					{!tag=public_suffix}public_suffix:("co.uk" OR "theregister.co.uk")
//					{!tag=domain}domain:("theregister.co.uk")
					Map<String, List<String>> filters = new HashMap<String, List<String>>();
					List<String> fqList = new ArrayList<String>();
					fqList.add(query.hostDomainPublicSuffix);
					filters.put("or-host", fqList);
					filters.put("or-domain", fqList);
					filters.put("or-public_suffix", fqList);
					processFilterQueries(solrParameters, filters, fq);
				}
//				processHostDomainPublicSuffix(solrParameters, query.hostDomainPublicSuffix);
//				processUrlHostDomainPublicSuffix(parameters, query.urlHostDomainPublicSuffix);
			} catch (ParseException e) {
				throw new SolrServerException(e);
			}
	
			Map<String, List<String>> params = query.parameters;
			// remaining parameters
			for(String key : params.keySet()) {
				Logger.info("remaining parameters: " + key + "=" + params.get(key).get(0));
				if (key.equals("facet.sort")) {
					// there's only one sort
					solrParameters.setFacetSort(params.get(key).get(0));
				} else if (key.contains(".facet.sort")) {
					if (!params.get(key).get(0).isEmpty()) {
						solrParameters.add(key, params.get(key).get(0));
						query.menu.put(key, params.get(key).get(0));
					}
				}
			}
			
			if (fq.size() > 0) {
				solrParameters.setFilterQueries(fq.toArray(new String[fq.size()]));
			}
			
			Logger.info("Query: " + solrParameters.toString());
			
			// Perform the query:
			QueryResponse res = solr.query(solrParameters);
			
			Logger.info("QTime: " + res.getQTime());
			Logger.info("Response Header: " + res.getResponseHeader());
			
			query.res = res;
			query.processQueryResponse();
			
		} catch(ParseException e) {
			throw new SolrServerException(e);
		}

//	    Iterator<SolrDocument> iter = query.res.getResults().iterator();
//
//	    while (iter.hasNext()) {
//	      SolrDocument resultDoc = iter.next();
//
//	      Object title = resultDoc.getFieldValue("title");
//	      Object subject = resultDoc.getFieldValue("subject");
//	      Object description = resultDoc.getFieldValue("description");
//	      Object comments = resultDoc.getFieldValue("comments");
//	      Object author = resultDoc.getFieldValue("author");
//	      Object url = resultDoc.getFieldValue("url");
//	      
//	      Logger.info("title: " + title);
//	      Logger.info("subject: " + subject);
//	      Logger.info("description: " + description);
//	      Logger.info("comments: " + comments);
//	      Logger.info("author: " + author);
//	      Logger.info("url: " + url);

//	      String id = (String) resultDoc.getFieldValue("id"); //id is the uniqueKey field
//	      Logger.info("id: " + id);

//	      if (query.res.getHighlighting().get(id) != null) {
//	        	Logger.info("title: " + query.res.getHighlighting().get(id).get("title"));
//	        	Logger.info("content: " + query.res.getHighlighting().get(id).get("content_text"));
//	      }
//	    }		
//		
//		Map<String, Map<String, List<String>>> highlights = query.res.getHighlighting();
//
//		for (Entry<String, Map<String, List<String>>> entry : highlights.entrySet()) {
//			Logger.info("Key : " + entry.getKey());
//			
//		}
		return query;
	}

	private List<String> processFilterQueries(SolrQuery solrParameters, Map<String, List<String>> filters, List<String> fq) {
		
		Logger.info("filters >>>>>>>> " + filters);
		StringBuilder filterBuilder = new StringBuilder();
		int filterCounter = 0;
		for (String filterKey : filters.keySet()) {
			
			String field = filterKey;
			// Excluded tags are ANDed together:
			if (filterKey.startsWith("-")) {
				field = field.replaceFirst("-", "");
				for (String val : filters.get(filterKey)) {
					if (val.isEmpty()) {
						Logger.info("No Value just filterKey: " + filterKey + " - "+ val);
						fq.add("{!tag=" + field + "}" + filterKey);
					} else {
						fq.add("{!tag=" + field + "}" + filterKey + ":" + val); // TODO
					}
				}
			} else if (filterKey.startsWith("or-")) {
				field = field.replaceFirst("or-", "");
//				{!tag=host}host:(theregister.co.uk)
				for (String val : filters.get(filterKey)) {
					if (filterCounter > 0) {
						filterBuilder.append(" OR ");
					}
					filterBuilder.append("{!tag=").append(field).append("}").append(field).append(":").append(val);
					filterCounter++;
				}
			} else {
				// Included tags are ORed together:
				String filter = "{!tag=" + field + "}" + filterKey + ":(";
				int counter = 0;
				for (String val : filters.get(filterKey)) {
					Logger.info("key: " + val);
					if (counter > 0)
						Logger.info("counter: " + counter);
						filter += " OR ";
					filter += "" + val + ""; // TODO Escape correctly?
					counter++;

				}
				filter += ")";
				fq.add(filter);
			}
		}
		if (filterBuilder.length() > 0) {
			fq.add(filterBuilder.toString());
		}
		Logger.info("OR >>> " + filterBuilder.toString());
		return fq;
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
			String currentQuery = parameters.getQuery();
			parameters.setQuery(currentQuery + " " + builder.toString());
			Logger.info("new query: " + parameters.getQuery());
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
		Logger.info("excluded: " + excluded);
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
			
//			{!tag=host}host:("theregister.co.uk")
//			{!tag=public_suffix}public_suffix:("co.uk" OR "theregister.co.uk")
//			{!tag=domain}domain:("theregister.co.uk")

			parameters.add("host", hostDomainPublicSuffix);
			parameters.add("domain", hostDomainPublicSuffix);
			parameters.add("public_suffix", hostDomainPublicSuffix);
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
