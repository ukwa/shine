/**
 * 
 */
package uk.bl.wa.shine;

import static java.lang.Math.abs;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse.Suggestion;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.ModifiableSolrParams;

import play.Logger;
import play.libs.Json;
import play.cache.Cache;
import uk.bl.wa.shine.exception.ShineException;
import uk.bl.wa.shine.model.FacetValue;
import uk.bl.wa.shine.model.SearchData;
import uk.bl.wa.shine.service.FacetService;
import uk.bl.wa.shine.service.FacetServiceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Shine extends Solr {

	private int perPage;
	private int csvMaxLimit;
	private int csvIntervalLimit;
	private String shards;
	private FacetService facetService = null;

	public Shine(play.Configuration config) {
		super(config);
		this.facetService = new FacetServiceImpl(config);
		this.perPage = config.getInt("per_page");
		this.csvMaxLimit = config.getInt("csv_max_limit");
		this.csvIntervalLimit = config.getInt("csv_interval_limit");
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

		solrParameters.setStart(start);
		// may make this configurable
		solrParameters.setFacetLimit(10);
		
		//solrParameters.setParam(FacetParams.FACET_METHOD, FacetParams.FACET_METHOD_enum);
		//solrParameters.setParam(FacetParams.FACET_ENUM_CACHE_MINDF, "25");
		
		// add shard is mode set to long
		if (StringUtils.isNotEmpty(query.mode) && StringUtils.equalsIgnoreCase(query.mode, "full") && StringUtils.isNotEmpty(shards)) {
			Logger.debug("FULL MODE");
			solrParameters.setParam("shards", shards);
		}
		
		Logger.debug("facet methods set");
		// Sorts:
		// parameters.setSort("sentiment_score", ORDER.asc);
		Logger.debug("params: " + solrParameters);
		
		return solrParameters;
	}

	public Query search(Query query) throws ShineException {
		Logger.debug("search: " + query.responseParameters);
		return this.search(query, perPage);
	}
	
	public Query search(Query query, int rows) throws ShineException {
		return this.search(query, rows, null);
	}
	
	public Query search(Query query, int rows, Integer start) throws ShineException {
		return this.search(query, buildInitialParameters(query), rows, start);
	}
	
	public Query browse(Query query) throws ShineException {
		return this.browse(query, buildInitialParameters(query));
	}

	public Query graph(Query query) throws ShineException {
		return this.graph(query, buildInitialParameters(query));
	}
	
	public int roundUp(int num, int divisor) {
	    int sign = (num > 0 ? 1 : -1) * (divisor > 0 ? 1 : -1);
	    return sign * (abs(num) + abs(divisor) - 1) / abs(divisor);
	}
	
	public List<SearchData> export(Query query) throws ShineException {
		Query q = this.search(query, 0);
		int total = (int)q.res.getResults().getNumFound();
		
		if (total > this.csvMaxLimit) {
			total = this.csvMaxLimit;
		}
		Logger.debug("get total: " + total);

//		Query exportList = this.search(query, this.csvIntervalLimit);
//		
//		int testTotal = (int)exportList.res.getResults().getNumFound();
//		
//		Logger.debug("Test Total: " + testTotal);

//		SolrQuery solrParameters = new SolrQuery(query.query);
//		solrParameters.setRows(this.csvIntervalLimit);
		
//		Logger.debug("solrParameters: " + solrParameters);
		
//		SolrQuery solrParameters = new SolrQuery(query.query);
//		solrParameters.setRows(this.csvIntervalLimit);
		
//		start 0
//		start=10
//		start 20
//		
//		&rows=10
//		0, 500, 1000, 1500
//		int times = total / this.csvIntervalLimit;
		int times = this.roundUp(total, this.csvIntervalLimit);
		
		int start = 0;
		
		List<SearchData> exportDataList = new ArrayList<SearchData>();
		// 61 / 1000 / 0 / 0
		Logger.debug(total + " / " +  this.csvIntervalLimit + "  = " + times + " ... start " + start);
		for (int i = 0; i < times; i++) {
//			solrParameters.setStart(start); // than increment
//			Query search = doSearch(query, solrParameters);			
			Query search = search(query, this.csvIntervalLimit, Integer.valueOf(start));
			Logger.debug("in chunks total: " + search.res.getResults().getNumFound());
			Logger.debug("Export Query: " + query.parameters);
			Logger.debug(total + " / " +  this.csvIntervalLimit + "  = " + times + " ... start " + start);
			for (SolrDocument document : search.res.getResults()) {
				String title = document.getFirstValue("title") == null ? "" : document.getFirstValue("title").toString();
				String host = document.getFirstValue("host") == null ? "" : document.getFirstValue("host").toString();
				String publicSuffix = document.getFirstValue("public_suffix") == null ? "" : document.getFirstValue("public_suffix").toString();
				String crawlYear = document.getFirstValue("crawl_year") == null ? "" : document.getFirstValue("crawl_year").toString();
				String contentTypeNorm = document.getFirstValue("content_type_norm") == null ? "" : document.getFirstValue("content_type_norm").toString();
				String contentLanguage = document.getFirstValue("content_language") == null ? "" : document.getFirstValue("content_language").toString();
				String crawlDate = document.getFirstValue("crawl_date") == null ? "" : document.getFirstValue("crawl_date").toString();
				String waybackDate = document.getFirstValue("wayback_date") == null ? "" : document.getFirstValue("wayback_date").toString();
				String url = document.getFirstValue("url") == null ? "" : document.getFirstValue("url").toString();
				SearchData searchData = new SearchData(title, host, publicSuffix, crawlYear, contentTypeNorm, contentLanguage, crawlDate, url, waybackDate);
				exportDataList.add(searchData);
			}
			start += this.csvIntervalLimit;
		}
		return exportDataList;
	}
	
	private Query search(Query query, SolrQuery solrParameters, int rows, Integer start) throws ShineException {
		
	    solrParameters.setHighlight(true).setHighlightSnippets(10); //set other params as needed
	    
	    solrParameters.setHighlightSimplePre("<em>");
	    solrParameters.setHighlightSimplePost("</em>");
	    
	    solrParameters.addHighlightField("content,title,url");
	    solrParameters.setParam("hl.mergeContiguous", "true");
	    solrParameters.setParam("hl.maxAnalyzedChars", "100000");
	    solrParameters.setParam("hl.usePhraseHighlighter", "true");
	    //solrParameters.setHighlightRequireFieldMatch(Boolean.TRUE);

		solrParameters.setRows(rows);

//		if (start != null) {
//			solrParameters.setStart(start);
//		}
		
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
//			Logger.debug("facet.sort: " + parameters.get("facet.sort"));
//			solrParameters.setFacetSort("index");
//			Logger.debug("set to index");
//		}
		
		Logger.debug("solrParameters: " + solrParameters);
		
//		boolean done = false;
//		String cursorMark = CursorMarkParams.CURSOR_MARK_START;
//		solrParameters.setSort(SortClause.asc("id"));

		Query q = null;
//		while (!done) {
//			solrParameters.set(CursorMarkParams.CURSOR_MARK_PARAM, cursorMark);
			q = doSearch(query, solrParameters);
//			String nextCursorMark = q.res.getNextCursorMark();
//			// process
//			if (cursorMark.equals(nextCursorMark)) {
//				done = true;
//			}
//			cursorMark = nextCursorMark;
//		}
		
		return q;
	}

	private Query browse(Query query, SolrQuery parameters) throws ShineException {
		// facets available on the advanced search fields
		Map<String, FacetValue> facetValues = new HashMap<String, FacetValue>();
		FacetValue collectionFacetValue = new FacetValue("collection", "Collection");
		FacetValue collectionsFacetValue = new FacetValue("collections", "Collections");
		facetValues.put(collectionFacetValue.getName(), collectionFacetValue);
		facetValues.put(collectionsFacetValue.getName(), collectionsFacetValue);
		// build up the facets and add to map to pass on 
		Logger.debug("browse facetValues: " + facetValues);
		parameters.setRows(perPage);
		query.facetValues = facetValues;
		return doSearch(query, parameters);
	}

	private Query graph(Query query, SolrQuery solrParameters) throws ShineException {

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
			query.yearStart = "1995";
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
		
		// Up to next year, or query.yearEnd:
		if (StringUtils.isEmpty(query.yearEnd)) {
			Calendar nowCal = Calendar.getInstance();
			query.yearEnd = String.valueOf(nowCal.get(Calendar.YEAR)+1);
		}
		cal.set(Calendar.YEAR, Integer.parseInt(query.yearEnd));
		Date end = cal.getTime();
		Logger.debug("start date: " + start);
		Logger.debug("end date: " + end);
		solrParameters.addDateRangeFacet("crawl_date", start, end, "+1YEAR");
		solrParameters.setFacetSort(FacetParams.FACET_SORT_INDEX);
		return doSearch(query, solrParameters);
	}

	// for advanced search using own facets
	private Query doSearch(Query query, SolrQuery solrParameters) throws ShineException {

		Logger.debug("pre query.responseParameters: " + query.responseParameters);
		
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
		    Logger.debug("actionParameters: " + actionParameters);
		    Logger.debug("parameters: " + parameters);
		    
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
					    Logger.debug("removing>>>> " + selectedFacetValue.getName());
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
					Logger.debug("excludes: " + query.getExcludes());
					for (String value : query.getExcludes()) {
						selected.append("AND -id:").append("\"").append(value).append("\"").append(" ");
					}
					Logger.debug("excluded: " + selected.toString().trim());
					
					// &q=wikipedia AND -id:"20080514125602/6B+cyN12vEfEOYgIzZDdw==" AND -id:"20100601200405/wTwHWZVx%2BiTLVo3g9ULPnA=="
//					StringBuilder selected = new StringBuilder();
					for (String value : query.getExcludeHosts()) {
						selected.append("AND -host:").append("\"").append(value).append("\"").append(" ");
					}
					Logger.debug("excludeHost: " + selected.toString().trim());
					
					if (StringUtils.isNotEmpty(selected.toString())) {
						q = q + " " + selected.toString().replaceFirst(Pattern.quote("AND"), "").trim();
					}
				}
		    }

			solrParameters.add("q", q);
			solrParameters.add("text", q);
			
			Map<String, FacetValue> facetValues = query.facetValues;
			
			// should get updated list of added/removed facet values
			Logger.debug("doSearch:facetValues: " + facetValues);
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
				
				processWebsiteTitle(solrParameters, query.websiteTitle);
				if (StringUtils.isNotEmpty(query.pageTitle)) {
					solrParameters.addFilterQuery("title:" + query.pageTitle);
				}
				if (StringUtils.isNotEmpty(query.author)) {
					solrParameters.addFilterQuery("author:" + query.author);
				}
				if (StringUtils.isNotEmpty(query.url)) {
					solrParameters.addFilterQuery("url:" + query.url);
				}
				if (StringUtils.isNotEmpty(query.fileFormat)) {
					solrParameters.addFilterQuery("content_type_norm:" + query.fileFormat);
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
				throw new ShineException(e);
			}
	
			Map<String, List<String>> params = query.parameters;
			// remaining parameters
			for(String key : params.keySet()) {
				Logger.debug("remaining parameters: " + key + "=" + params.get(key).get(0));
				if (key.equals("facet.sort")) {
					// there's only one sort
					solrParameters.setFacetSort(params.get(key).get(0));
				} else if (key.contains(".facet.sort")) {
					if (!params.get(key).get(0).isEmpty()) {
						solrParameters.add(key, params.get(key).get(0));
						query.menu.put(key, params.get(key).get(0));
					}
				} else {
					
				}
			}
			
			if (fq.size() > 0) {
				solrParameters.addFilterQuery(fq.toArray(new String[fq.size()]));
			}
			
			// Check the cache:
			String qkey = "solr-query/"+solrParameters.toString();
			Logger.info("Checking cache under key: "+qkey);
			QueryResponse res = (QueryResponse) Cache.get(qkey);
			// Perform the query if not cached:
			if( res == null ) {
				Logger.info("Cache miss, so running query... /select?" + solrParameters.toString());
				res = getSolrServer().query(solrParameters);
				// Cache for an hour:
				Cache.set(qkey, res, 60 * 60);
				Logger.debug("QTime: " + res.getQTime());
				Logger.debug("Response Header: " + res.getResponseHeader());
			} else {
				Logger.info("Cache hit!");
			}
			// Post-process:
			query.res = res;
			Logger.debug("query.responseParameters: " + query.responseParameters);
			query.processQueryResponse();
			
		} catch(ParseException | SolrServerException e) {
			throw new ShineException(e);
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
//	      Logger.debug("title: " + title);
//	      Logger.debug("subject: " + subject);
//	      Logger.debug("description: " + description);
//	      Logger.debug("comments: " + comments);
//	      Logger.debug("author: " + author);
//	      Logger.debug("url: " + url);

//	      String id = (String) resultDoc.getFieldValue("id"); //id is the uniqueKey field
//	      Logger.debug("id: " + id);

//	      if (query.res.getHighlighting().get(id) != null) {
//	        	Logger.debug("title: " + query.res.getHighlighting().get(id).get("title"));
//	        	Logger.debug("content: " + query.res.getHighlighting().get(id).get("content_text"));
//	      }
//	    }		
//		
//		Map<String, Map<String, List<String>>> highlights = query.res.getHighlighting();
//
//		for (Entry<String, Map<String, List<String>>> entry : highlights.entrySet()) {
//			Logger.debug("Key : " + entry.getKey());
//			
//		}
		return query;
	}

	private List<String> processFilterQueries(SolrQuery solrParameters, Map<String, List<String>> filters, List<String> fq) {
		
		Logger.debug("filters >>>>>>>> " + filters);
		StringBuilder filterBuilder = new StringBuilder();
		int filterCounter = 0;
		for (String filterKey : filters.keySet()) {
			
			String field = filterKey;
			// Excluded tags are ANDed together:
			if (filterKey.startsWith("-")) {
				field = field.replaceFirst("-", "");
				for (String val : filters.get(filterKey)) {
					if (val.isEmpty()) {
						Logger.debug("No Value just filterKey: " + filterKey + " - "+ val);
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
					Logger.debug("key: " + val);
					if (counter > 0)
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
		Logger.debug("OR >>> " + filterBuilder.toString());
		return fq;
	}
	
	private Date parseDateString(String dateString) throws ParseException {
		if (StringUtils.isNotEmpty(dateString)) {
			SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
			// chrome issue
			if (dateString.contains("-")) {
				formatter = new SimpleDateFormat("yyyy-MM-dd");
				Logger.debug("chrome data format for " + dateString);
			}
			return formatter.parse(dateString);
		}
		return null;

	}

	private void processDateRange(SolrQuery parameters, String dateStart,
			String dateEnd) throws ParseException {
		Logger.debug("processDateRange: " + dateStart + " - " + dateEnd);
		Date dateObjStart = parseDateString(dateStart);
		Date dateObjEnd = parseDateString(dateEnd);
		Logger.debug("dateStart: " + dateStart + " dateEnd: " + dateEnd);
		if (dateObjStart != null && dateObjEnd != null) {
			SimpleDateFormat formatter = new SimpleDateFormat(
					"yyyy-MM-dd'T'HH:mm:ss'Z'");
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
			Logger.debug("builder parameters: " + builder.toString());
			String currentQuery = parameters.getQuery();
			parameters.setQuery(currentQuery + " " + builder.toString());
			Logger.debug("new query: " + parameters.getQuery());
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
		Logger.debug("excluded: " + excluded);
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
			QueryResponse response = getSolrServer().query(params);
			SpellCheckResponse spellCheckResponse = response
					.getSpellCheckResponse();

			Logger.debug("spellCheckResponse: " + spellCheckResponse);

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
			Logger.debug("result: " + result);
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
