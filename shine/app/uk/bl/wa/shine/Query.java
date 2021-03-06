/**
 * 
 */
package uk.bl.wa.shine;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import uk.bl.wa.shine.exception.ShineException;
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

	public String author;
	
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
	
	public String collection;

	public Integer page;
	
	public String sort;
	
	public String order;
	
	public String mode;
	
	// incoming parameters
	public Map<String, List<String>> parameters;
	
	private List<String> selectedResources;

	private List<String> excludes;
	
	private List<String> excludeHosts;

	public Map<String,String> menu;
	
	public Query() {}
	
	public Query(String query) throws ShineException {
		this.query = query;
		this.init();
	}
	
	public Query(String query, Map<String,List<String>> parameters) throws ShineException {
		this.query = query;
		this.parameters = parameters;
		this.init();
	}
	
	public Query(String query, String proximityPhrase1, String proximityPhrase2, String proximity, 
			String exclude, String dateStart, String dateEnd, String url, String hostDomainPublicSuffix, 
		    String fileFormat, String websiteTitle, String pageTitle, String author, String collection, Map<String,List<String>> parameters, String mode) throws ShineException {
		this.query = query;
		this.proximity = new Proximity();
		this.proximity.setPhrase1(proximityPhrase1);
		this.proximity.setPhrase2(proximityPhrase2);
		this.proximity.setProximity(proximity);
		this.excluded = exclude;
		this.dateStart = dateStart;
		this.dateEnd = dateEnd;
		this.url = url;
		this.hostDomainPublicSuffix = hostDomainPublicSuffix;
		this.fileFormat = fileFormat;
		this.websiteTitle = websiteTitle;
		this.pageTitle = pageTitle;
		this.author = author;
		this.collection = collection;
		this.parameters = parameters;
		this.mode = mode;
		Logger.debug("Pre Init: " + this.responseParameters);
		this.init();
		Logger.debug("Query: " + this.responseParameters);
	}

	private void init() throws ShineException {
		this.facets = new ArrayList<String>();
		this.facetValues = new HashMap<String, FacetValue>();
		this.selectedResources = new ArrayList<String>();
		this.excludes = new ArrayList<String>();
		this.excludeHosts = new ArrayList<String>();
		this.menu = new HashMap<String,String>();
		this.parseParameters();
	}
	
	private String encodeParameter(String parameter) throws ShineException {
		try {
			parameter = URLEncoder.encode(parameter, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new ShineException(e);
		}
		return parameter;
	}
	
	private void parseParameters() throws ShineException {
		Logger.info("parseParams: " + this.parameters);
		filters = new HashMap<String, List<String>>();
		StringBuilder responseParameters = new StringBuilder("");

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
//				    filters.put(param, values);
//					Logger.info(" facet other values: " + values);
				} else if (param.equals("facet.fields")) {
					for (String value : values) {
						facets.add(value);
						if (StringUtils.isNotBlank(value)) {
							responseParameters.append("&facet.fields=").append(value);
						}
					}
				}
			}
		}
		
		if (parameters.get("selectedResource") != null) {
			Iterator<String> iterator = parameters.get("selectedResource").iterator();
			while (iterator.hasNext()) {
				String selectedResource = encodeParameter(iterator.next().trim());
				Logger.info("selectedResource >>>" + selectedResource);
				selectedResources.add(selectedResource);
				if (StringUtils.isNotBlank(selectedResource)) {
					responseParameters.append("&selectedResource=").append(selectedResource);
				}
			}
		}
		
		if (parameters.get("exclude") != null) {
			Iterator<String> iterator = parameters.get("exclude").iterator();
			while (iterator.hasNext()) {
				String exclude = encodeParameter(iterator.next().trim());
				Logger.info("exclude >>>" + exclude);
				try {
					exclude = URLDecoder.decode(exclude,"UTF-8");
					String[] values = exclude.split(";;;");
					String id = values[0].trim();
					excludes.add(id);
					if (StringUtils.isNotBlank(id)) {
						responseParameters.append("&exclude=").append(exclude);
					}
				} catch (UnsupportedEncodingException e) {
					throw new ShineException(e);
				}
			}
		}

		if (parameters.get("excludeHost") != null) {
			Iterator<String> iterator = parameters.get("excludeHost").iterator();
			while (iterator.hasNext()) {
				String excludeHost = encodeParameter(iterator.next().trim());
				Logger.info("excludeHost >>>" + excludeHost);
				excludeHosts.add(excludeHost);
				if (StringUtils.isNotBlank(excludeHost)) {
					responseParameters.append("&excludeHost=").append(excludeHost);
				}
			}
		}
		
		// non facets
		if (parameters.get("year_start") != null) {
			yearStart = parameters.get("year_start").get(0);
		}
		if (parameters.get("year_end") != null) {
			yearEnd = Integer.toString(Integer.parseInt(parameters.get("year_end").get(0)) + 1);
		}
		Logger.info("Dates: " + yearStart + " " + yearEnd);
		
		if (parameters.get("page") != null) {
			page = Integer.parseInt(parameters.get("page").get(0));
		} else {
			page = 1;
		}
		if (parameters.get("sort") != null) {
			sort = parameters.get("sort").get(0);
			if (StringUtils.isNotBlank(sort)) {
				Logger.debug("sort: " + sort);
				responseParameters.append("&sort=").append(sort);
			}
		}
		if (parameters.get("order") != null) {
			order = parameters.get("order").get(0);
			if (StringUtils.isNotBlank(order)) {
				responseParameters.append("&order=").append(order);
			}
		}

		Logger.info("datestart >>>> " + parameters.get("dateStart"));
		if (parameters.get("dateStart") != null) {
			dateStart = parameters.get("dateStart").get(0).replace("\"", "");
			Logger.info("changing date: " + dateStart);
		}
		if (parameters.get("dateEnd") != null) {
			dateEnd = parameters.get("dateEnd").get(0).replace("\"", "");
		}
		
		List<String> invert = this.parameters.get("invert");
		if (invert != null) {
			for (String inv : invert) {
				if (StringUtils.isNotEmpty(inv)) {
					responseParameters.append("&invert=").append(inv);
				}
			}
		}
		this.responseParameters =  responseParameters.toString();
		Logger.info("parseParameters: " + responseParameters);
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
		
		Logger.info("pre responseParameters: " + this.responseParameters);

		this.responseParameters += this.responseFacetParameters();
		
		// should only be for advanced search
		this.responseParameters += processAdvancedSearchParameters();

//		StringBuilder parameters = new StringBuilder("");
//		
//		this.responseParameters += parameters.toString();
		
		Logger.info("updated responseParameters: " + this.responseParameters);
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

	public List<String> getSelectedResources() {
		return selectedResources;
	}

	public List<String> getExcludes() {
		return excludes;
	}

	public List<String> getExcludeHosts() {
		return excludeHosts;
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
						String out = "&facet.out.";
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
		
		// inverting stuff?
		Logger.debug("processFacetsAsParamValues: " + parameters.toString());
		
		return parameters.toString();
	}
	
	private String processAdvancedSearchParameters() {
		StringBuilder parameters = new StringBuilder("");
		if (StringUtils.isNotEmpty(websiteTitle))
			parameters.append("&websiteTitle=").append(websiteTitle);
		if (StringUtils.isNotEmpty(pageTitle))
			parameters.append("&pageTitle=").append(pageTitle);
		if (StringUtils.isNotEmpty(author))
			parameters.append("&author=").append(author);
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
		if (proximity != null) {
			if (StringUtils.isNotEmpty(proximity.getPhrase1()))
				parameters.append("&proximityPhrase1=").append(proximity.getPhrase1());
			if (StringUtils.isNotEmpty(proximity.getPhrase2()))
				parameters.append("&proximityPhrase2=").append(proximity.getPhrase2());
			if (StringUtils.isNotEmpty(proximity.getProximity()))
				parameters.append("&proximity=").append(proximity.getProximity());
		}
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
