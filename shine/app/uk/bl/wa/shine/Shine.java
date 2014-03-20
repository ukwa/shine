/**
 * 
 */
package uk.bl.wa.shine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

	private List<FacetValue> facets = null;
	
	private Map<String,String> facet_names = null;
	
	private Map<String,List<String>> facets_tree = null;
		
	private int perPage; 
	
	private FacetService facetService = null;
	
	public Shine( play.Configuration config ) {
		 super(config);
		 
		 this.facetService = new FacetServiceImpl();
		 this.facets = new ArrayList<FacetValue>();
		 this.facet_names = new HashMap<String,String>();
		 this.facets_tree = new LinkedHashMap<String,List<String>>();

		 List<FacetValue> facetValues = new ArrayList<FacetValue>();
		 Map<String, Object> map = config.getConfig("facets").asMap();
		 for (String facetHeader : map.keySet()) {
			 @SuppressWarnings("unchecked")
			Map<String, String> values = (Map<String, String>) map.get(facetHeader);
			 for (String key : values.keySet()) {
				 String value = values.get(key);
				 FacetValue facetValue = new FacetValue(key, value);
//				 Logger.info("facetValue: " + facetValue.getName() + "=" + facetValue.getValue());
				 facetValues.add(facetValue);
				 // current stuff
				 // Also store in a flat list:
				 this.facets.add(facetValue);
			 }
			 facetService.add(facetHeader, facetValues);
		 }
//		 Logger.info("Map>>> " + FacetServiceImpl.INSTANCE.getMap());
		 for( String fc : config.getConfig("facets").subKeys() ) {
			 List<String> fl = new ArrayList<String>();
			 for( String f : config.getConfig("facets."+fc).subKeys() ) {
//				 Logger.info("facet value: " + f);
				 fl.add(f);
				 // Also store in a flat list:
//				 this.facets.add(f);
				 // Also store the name:
				 this.facet_names.put(f,config.getString("facets."+fc+"."+f));
			 }
//			 Logger.info("Putting "+fc+" > "+fl);
			 this.facets_tree.put(fc, fl);
		 }
		 this.perPage = config.getInt("per_page");
    }
	
	public QueryResponse search(String query, Map<String,List<String>> params) throws SolrServerException {
		return this.search(query, params, null);
	}

	public QueryResponse search(String query, Map<String,List<String>> params, int pageNo, String sort, String order) throws SolrServerException {
		
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
		//parameters.setSort("sentiment_score", ORDER.asc);
		QueryResponse res = this.search(query, params, parameters);
		return res;
	}

	public QueryResponse search(String query, Map<String,List<String>> params, SolrQuery parameters) throws SolrServerException {
		
		Logger.info("search parameters: " + params);
		Logger.info("facetService: " + facetService + " - " + facetService.getDefaultList() + " - " + facetService.getMap());

		if (parameters == null) parameters = new SolrQuery();
		// The query:
		parameters.set("q", query);
		// calculate increments based on per_page

		for (FacetValue facetValue : facetService.getList()) {
			parameters.addFacetField("{!ex="+facetValue.getName()+"}"+facetValue.getName());
		}

		parameters.setFacetMinCount(1);
		List<String> fq = new ArrayList<String>();
		for( String param : params.keySet() ) {
			String field = param;
			if ( param.equals("facet.sort")) {
				// there's only one sort
				parameters.setFacetSort(params.get(param).get(0));
			}
			// Excluded tags are ANDed together:
			else if( param.startsWith("-")) {
				field = field.replaceFirst("-", "");
				for( String val : params.get(param)) {
					fq.add("{!tag="+field+"}"+param+":"+val); // TODO Escape correctly?
				}
			} else {
				// Included tags are ORed together:
				String filter = "{!tag="+field+"}"+param+":(";
				int counter = 0;
				for( String val : params.get(param)) {
					if( counter > 0 ) filter += " OR ";
				    filter += ""+val+"";							  // TODO Escape correctly?
				    counter++;
				    
				}
				filter += ")";
				fq.add(filter);
			}			
		}
		if( fq.size() > 0 ) {
			parameters.setFilterQueries(fq.toArray(new String[fq.size()]));
		}

		// Paging:
		parameters.setRows(perPage);
		Logger.info("Query: "+parameters.toString());
		// Perform the query:
		QueryResponse res = solr.query(parameters);
		Logger.info("QTime: "+res.getQTime());
		Logger.info("Response Header: "+res.getResponseHeader());
		return res;
	}

    public JsonNode suggest(String name) throws SolrServerException {
    	
    	JsonNode jsonData = null;
        List<ObjectNode> result = new ArrayList<ObjectNode>();
        JsonNodeFactory nodeFactory = new JsonNodeFactory(false);

    	try {
	    	ModifiableSolrParams params = new ModifiableSolrParams();
	    	params.set("qt", "/suggest");
	    	params.set("q", name);
	    	params.set("wt", "json");
	    	QueryResponse response = solr.query(params);
	        SpellCheckResponse spellCheckResponse = response.getSpellCheckResponse() ;
	        
			Logger.info("spellCheckResponse: " + spellCheckResponse);

	        List<Suggestion> suggestions = spellCheckResponse.getSuggestions();
	
	        if (suggestions != null && suggestions.size() > 0) {
	        	for(Suggestion suggestion : suggestions) {
	               List<String> alternatives = suggestion.getAlternatives() ;
	               if (alternatives != null && alternatives.size() > 0) {
	            	   for(String alternative : alternatives) {
	            		   ObjectNode child = nodeFactory.objectNode();
	            		   child.put("title", alternative);
	            		   result.add(child);
	                   }
	               }
	        	}
	        }
	    	jsonData = Json.toJson(result);
    	} catch (Exception e) {
//    		throw new SolrServerException("Suggestions not found: " + e);
    		ObjectNode testChild = nodeFactory.objectNode();
    		testChild.put("title", "Suggestions server isn't working at present");
    		result.add(testChild);
    		Logger.info("result: " + result);
	    	jsonData = Json.toJson(result);
    	}
    	return jsonData;
    }
	
	private String temp( String query ) throws SolrServerException {
		QueryResponse res = this.search(query, null, 0, null, null);
		res.getFacetFields().get(0).getValues().get(0).getName();
		res.getResults().get(0).getFirstValue("title");
		res.getResults().getNumFound();
		return null;
	}

	public Map<String, List<String>> getFacets_tree() {
		return facets_tree;
	}

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
