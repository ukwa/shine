/**
 * 
 */
package uk.bl.wa.shine;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;

import play.Logger;
import uk.bl.wa.shine.model.FacetValue;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class Query {

	public static final String FACET_SORT_INDEX = "index";
	public static final String FACET_SORT_COUNT = "count";
	
	public String query; // Full text
	
	public Map<String,List<String>> filters;
	
	public QueryResponse res;
	
	public String responseParameters;

	public Map<String, FacetValue> facetValues;
	
	public List<String> facets;
	
	public String websiteTitle;
	
	public String pageTitle;

	public String name;
	
	public String url;
	
	public String fileFormat;
	
	public String dateStart; // (this should select a date range for the crawl_date or crawl_dates field).
	
	public String dateEnd;
	
	public String yearStart;
	
	public String yearEnd;
	
	public String excluded;
	
	public Proximity proximity;
	
	public String hostDomainPublicSuffix;
	
	public String urlHostDomainPublicSuffix;

	public Integer page;
	
	public String sort;
	
	public String order;
	
	private Map<String, List<String>> parameters;
	
	public Query(String query, Map<String,List<String>> parameters) {
		facets = new ArrayList<String>();
		this.parameters = parameters;
		facetValues = new HashMap<String, FacetValue>();
		this.query = query;
		this.proximity = new Proximity();
		this.parseParameters();
	}
	
	private void parseParameters() {
		Logger.info("parseParams: " + this.parameters);
		filters = new HashMap<String, List<String>>();
		for( String param : parameters.keySet() ) {
			List<String> values = parameters.get(param);
			if (!values.isEmpty()) {
				if( param.startsWith("facet.in.") && values.get(0).length() > 0) {
					filters.put(param.replace("facet.in.", ""), values);
					Logger.info(" facet in values: " + values);
				} else if( param.startsWith("facet.out.") && values.get(0).length() > 0) {
				    filters.put("-"+param.replace("facet.out.", ""), values);
					Logger.info(" facet out values: " + values);
				} else if( param.equals("facet.sort")  && values.get(0).length() > 0) {
				    filters.put(param, values);
					Logger.info(" facet other values: " + values);
				} else if (param.equals("facet.fields")) {
					for (String value : values) {
						facets.add(value);
					}
				}
			}
		}
		
		Logger.info("facets: " + facets);
		Logger.info("filters: " + filters);
		
		if (parameters.get("facet.sort") != null) {
			String facetSort = parameters.get("facet.sort").get(0);
			Logger.info("facetSort: " + facetSort);
		}
		
		// non facets

		if (parameters.get("year_start") != null) {
			yearStart = parameters.get("year_start").get(0);
		}
		if (parameters.get("year_end") != null) {
			yearEnd = parameters.get("year_end").get(0);
		}
		Logger.info("Dates: " + yearStart + " " + yearEnd);
		
		if (parameters.get("page") != null) {
			page = Integer.parseInt(parameters.get("page").get(0));
		} else {
			page = 1;
		}
		if (parameters.get("sort") != null) {
			sort = parameters.get("sort").get(0);
		}
		if (parameters.get("order") != null) {
			order = parameters.get("order").get(0);
		}
		
		if (parameters.get("websiteTitle") != null) {
			websiteTitle = parameters.get("websiteTitle").get(0);
		}
		if (parameters.get("pageTitle") != null) {
			pageTitle = parameters.get("pageTitle").get(0);
		}
		if (parameters.get("name") != null) {
			name = parameters.get("name").get(0);
		}
		if (parameters.get("url") != null) {
			url = parameters.get("url").get(0);
		}
		if (parameters.get("fileFormat") != null) {
			fileFormat = parameters.get("fileFormat").get(0);
		}
		if (parameters.get("proximity") != null) {
			proximity = new Proximity();
			proximity.setPhrase1(parameters.get("proximity").get(0));
			proximity.setPhrase2(parameters.get("proximity").get(1));
			proximity.setProximity(parameters.get("proximity").get(2));
			Logger.info("" + proximity.getPhrase1() + " " + proximity.getPhrase2() + " " + proximity.getProximity());
		}
		if (parameters.get("datestart") != null) {
			dateStart = parameters.get("datestart").get(0).replace("\"", "");
		}
		if (parameters.get("dateend") != null) {
			dateEnd = parameters.get("dateend").get(0).replace("\"", "");
		}
		if (parameters.get("excluded") != null) {
			excluded = parameters.get("excluded").get(0);
		}		
		if (parameters.get("hostDomainPublicSuffix") != null) {
			hostDomainPublicSuffix = parameters.get("hostDomainPublicSuffix").get(0);
		}
		if (parameters.get("urlHostDomainPublicSuffix") != null) {
			urlHostDomainPublicSuffix = parameters.get("urlHostDomainPublicSuffix").get(0);
		}
	}
	
	public String getCheckedInString(String facet_name, String value ) {
		for( String fc : filters.keySet() ) {
			if( fc.equals(facet_name) && filters.get(fc).contains("\""+value+"\"")) {
				return "checked=''";
			}
		}
		return "";
	}
	
	public String getCheckedOutString(String facet_name, String value ) {
		return this.getCheckedInString("-"+facet_name, value);
	}
	
	public String getParamsPlusFilter(String facet_name, String facet_value) {
		String qp = "";
//		Logger.info("---- ----");
//		for( String key : res.getFacetQuery().keySet() ) {
////			Logger.info(key+">"+res.getFacetQuery().get(key));
//		}
//		for( FacetField fc: res.getLimitingFacets() ) {
////			Logger.info("LF: "+fc);
//		}
//		for( FacetField fc : this.res.getFacetFields() ) {
////			Logger.info("FF: "+fc);
//			if( fc.getName().equals(facet_name) ) {
//				
//			}
//		}
		return qp;
	}

	// Formatters	

	// Allow for pretty formatting of facet values:
	public String formatFacet( FacetField fc, Count f ) {
		if( "content_first_bytes".equals(fc.getName()) )
			return this.formatHexString(f.getName());
		if( "content_ffb".equals(fc.getName()) )
			return this.formatHexString(f.getName());
		return f.getName();
	}

	// Format numbers with commas:
	public String formatNumber( long number ) {
		NumberFormat numberFormat = new DecimalFormat("#,###");
		return numberFormat.format(number);
	}
	
	// Hex to string:
	// TODO Moving the HTML encoding (below) into templates.
	public String formatHexString( String hex ) {
		hex = hex.replaceAll(" ", "");
		try {
			byte[] bytes = Hex.decodeHex(hex.toCharArray());
			hex = this.partialHexDecode(bytes);
		} catch (DecoderException e) {
			Logger.error("Hex decode failed: "+e);
		} catch (UnsupportedEncodingException e) {
			Logger.error("Hex to UTF-8 recoding failed: "+e);
		}
		return hex;
	}
	
	public String getFacetValue(String facet_name) {
		if (filters.get(facet_name) != null) {
			return filters.get(facet_name).get(0);
		}
		return "";
	}
	
	public String getCheckedFacet(String facet_name) {
		if (StringUtils.isNotBlank(this.getFacetValue(facet_name))) {
			return "checked=''";
		}
		return "";
	}
	
	public String getFacetSortValue(String facet_name) {
		// only return the correct facet sort values
		if (facet_name.equals("facet.sort")) {
			return getFacetValue(facet_name);
		}
		return "";
	}
	
	@SuppressWarnings("unchecked")
	public void processQueryResponse() throws ParseException {
		

		this.responseParameters = this.responseFacetParameters();
		
		// process advance search parameters

		this.responseParameters += processAdvancedSearchParameters();
		
		// 1980-01-01T12:00:00Z
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd'T'hh:mm:ss'Z'");
		Calendar cal = Calendar.getInstance();
		List<RangeFacet.Count> counts = null;
		
		if (StringUtils.isNotEmpty(yearStart) && StringUtils.isNotBlank(yearEnd)) {
			dateStart = yearStart;
			dateEnd = yearEnd;
		}
//		
//		if (res.getFacetFields() != null) {
//			for (FacetField facetField : res.getFacetFields()) {
//				if (facetField.getName().equals("crawl_year")) {
//					List<FacetField.Count> fieldCounts = facetField.getValues();
//					if (!fieldCounts.isEmpty()) {
//						for (FacetField.Count count : fieldCounts) {
//							Logger.info("ff count >>> " + count.getName() + " " + count.getCount());
//						}
//						
//						FacetField.Count first = fieldCounts.get(0);
//						if (StringUtils.isEmpty(dateStart)) {
//							dateStart = first.getName();
//						}
//						FacetField.Count last = fieldCounts.get(fieldCounts.size()-1);
//						if (StringUtils.isEmpty(dateEnd)) {
//							dateEnd = last.getName();
//						}
//						Logger.info("first >>>> " + dateStart);
//						Logger.info("last >>>> " + dateEnd);
//					}
//				}
//			}
//		}
		
		if (res.getFacetRanges() != null) {
			for (RangeFacet<String, RangeFacet.Count> range : res.getFacetRanges()) {
				counts  = range.getCounts();
				ListIterator<RangeFacet.Count> listItr = counts.listIterator();
				// remove the empties
				while(listItr.hasNext()){
					RangeFacet.Count count = listItr.next();
					// remove
					if (count.getCount() == 0) {
						listItr.remove();
					}
				}
			}
			
			sdf = new SimpleDateFormat("yyyy");
			if (counts != null && counts.size() > 0) {
				RangeFacet.Count first = counts.get(0);
				Date firstDate = sdf.parse(first.getValue());
				cal.setTime(firstDate);
	//			cal.roll(Calendar.YEAR, false);
				if (StringUtils.isEmpty(dateStart)) {
					dateStart = sdf.format(cal.getTime());
				}
	
				RangeFacet.Count last = counts.get(counts.size()-1);
				Date lastDate = sdf.parse(last.getValue());
				cal.setTime(lastDate);
	//			cal.roll(Calendar.YEAR, true);
				if (StringUtils.isEmpty(dateEnd)) {
					dateEnd = sdf.format(cal.getTime());
				}
			}
			Logger.info("dates: " + dateStart + " - " + dateEnd);
		}
	}

	private String responseFacetParameters() {
		StringBuilder parameters = new StringBuilder("");
		if (res.getFacetFields() != null) {
			for (FacetField facetField : res.getFacetFields()) {
				for (Count count : facetField.getValues()) {
					String facet = facetField.getName() + "=\"" + count.getName() + "\"";
					if (StringUtils.isNotBlank(this.getCheckedInString(facetField.getName(),count.getName()))) {
						String in = "&facet.in."; 
						parameters.append(in).append(facet);
					} else if (StringUtils.isNotBlank(this.getCheckedOutString(facetField.getName(),count.getName()))) {
						String out = "&facet.out";
						parameters.append(out).append(facet);
					}
				}
			}
		}
			
		String facetSort = "facet.sort";
		String checked = getCheckedFacet(facetSort);
		if (StringUtils.isNotBlank(checked)) {
			String sortValue = getFacetSortValue(facetSort);
			parameters.append("&").append(facetSort).append("=").append(sortValue);
		}
		Logger.info("processFacetsAsParamValues: " + parameters.toString());
		
		return parameters.toString();
	}
	
	private String processAdvancedSearchParameters() {
		StringBuilder parameters = new StringBuilder("");
		if (StringUtils.isNotEmpty(websiteTitle))
			parameters.append("&websiteTitle=").append(websiteTitle);
		if (StringUtils.isNotEmpty(pageTitle))
			parameters.append("&pageTitle=").append(pageTitle);
		if (StringUtils.isNotEmpty(name))
			parameters.append("&name=").append(name);
		if (StringUtils.isNotEmpty(url))
			parameters.append("&url=").append(url);
		if (StringUtils.isNotEmpty(fileFormat))
			parameters.append("&fileFormat=").append(fileFormat);
		if (StringUtils.isNotEmpty(dateStart))
			parameters.append("&dateStart=").append(dateStart);
		if (StringUtils.isNotEmpty(dateEnd))
			parameters.append("&dateEnd=").append(dateEnd);
		if (StringUtils.isNotEmpty(excluded))
			parameters.append("&excluded=").append(excluded);
		if (StringUtils.isNotEmpty(proximity.getPhrase1()))
			parameters.append("&proximity=").append(proximity.getPhrase1());
		if (StringUtils.isNotEmpty(proximity.getPhrase2()))
			parameters.append("&proximity=").append(proximity.getPhrase2());
		if (StringUtils.isNotEmpty(proximity.getProximity()))
			parameters.append("&proximity=").append(proximity.getProximity());
		if (StringUtils.isNotEmpty(hostDomainPublicSuffix))
			parameters.append("&hostDomainPublicSuffix=").append(hostDomainPublicSuffix);
		//parameters.append("urlHostDomainPublicSuffix=").append(urlHostDomainPublicSuffix);

		return parameters.toString();
	}
	
	private String partialHexDecode( byte[] bytes ) throws UnsupportedEncodingException {
		String myString = new String( bytes, "ASCII");
		StringBuilder newString = new StringBuilder(myString.length());
		for (int offset = 0; offset < myString.length();)
		{
		    int codePoint = myString.codePointAt(offset);
		    offset += Character.charCount(codePoint);

		    // Replace invisible control characters and unused code points
		    switch (Character.getType(codePoint))
		    {
		        case Character.CONTROL:     // \p{Cc}
		        case Character.FORMAT:      // \p{Cf}
		        case Character.PRIVATE_USE: // \p{Co}
		        case Character.SURROGATE:   // \p{Cs}
		        case Character.UNASSIGNED:  // \p{Cn}
		            newString.append("<i class=\"hex\">");
		            newString.append(Hex.encodeHexString(new byte[] {Byte.valueOf((byte) codePoint) } ));
		            newString.append("</i>");
		            break;
		        default:
		            newString.append("<span class=\"lit\">");
		            newString.append(Character.toChars(codePoint));
		            newString.append("</span>");
		            break;
		    }
		}
		return newString.toString();
	}

	public Map<String, List<String>> getParameters() {
		return parameters;
	}
}
