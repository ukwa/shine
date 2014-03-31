/**
 * 
 */
package uk.bl.wa.shine;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.QueryResponse;

import play.Logger;
import uk.bl.wa.shine.model.FacetValue;

/**
 * @author Andrew Jackson <Andrew.Jackson@bl.uk>
 *
 */
public class Query {

	public static final String FACET_SORT_INDEX = "index";
	public static final String FACET_SORT_COUNT = "count";
	
	public String query;
	
	public Map<String,List<String>> filters;
	
	public QueryResponse res;
	
	public String facetParameters;

	public Map<String, FacetValue> facets;

	public String dateStart;
	
	public String dateEnd;
	
	public String excluded;
	
	public Proximity proximity;
	
    // The text-field should match the values in the 'host', 'domain' or 'public_suffix' fields.
	public String facetField;
	// The text-field should match the values in the 'url', 'host', 'domain' or 'public_suffix' fields.
	public String facetFieldValue;

	
	public Query(String query, Map<String,List<String>> params) {
		facets = new HashMap<String, FacetValue>();
		this.query = query;
		this.proximity = new Proximity();
		this.parseParams(params);
	}
	
	public void parseParams(Map<String,List<String>> params) {
		Logger.info("parseParams: " + params);
		filters = new HashMap<String, List<String>>();
		for( String param : params.keySet() ) {
			if( param.startsWith("facet.in.")) {
			    filters.put(param.replace("facet.in.", ""), params.get(param));
			} else if( param.startsWith("facet.out.")) {
			    filters.put("-"+param.replace("facet.out.", ""), params.get(param));
			} else if( param.equals("facet.sort") ) {
			    filters.put(param, params.get(param));
			}
		}
		
		Logger.info("parseParams: " + filters);
		
		if (params.get("datestart") != null) {
			dateStart = params.get("datestart").get(0);
		}
		if (params.get("dateend") != null) {
			dateEnd = params.get("dateend").get(0);
		}
		if (params.get("excluded") != null) {
			excluded = params.get("excluded").get(0);
		}
		if (params.get("proximity") != null) {
			proximity = new Proximity();
			proximity.setPhrase1(params.get("proximity").get(0));
			proximity.setPhrase2(params.get("proximity").get(1));
			proximity.setProximity(params.get("proximity").get(2));
			Logger.info("" + proximity.getPhrase1() + " " + proximity.getPhrase2() + " " + proximity.getProximity());
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
		for( String key : res.getFacetQuery().keySet() ) {
//			Logger.info(key+">"+res.getFacetQuery().get(key));
		}
		for( FacetField fc: res.getLimitingFacets() ) {
//			Logger.info("LF: "+fc);
		}
		for( FacetField fc : this.res.getFacetFields() ) {
//			Logger.info("FF: "+fc);
			if( fc.getName().equals(facet_name) ) {
				
			}
		}
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
	
	public void processQueryResponse(QueryResponse response) {
		this.res = response;
		this.processFacetsAsParamValues();
	}

	private void processFacetsAsParamValues() {
		StringBuilder parameters = new StringBuilder("");
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
		String facetSort = "facet.sort";
		String checked = getCheckedFacet(facetSort);
		if (StringUtils.isNotBlank(checked)) {
			String sortValue = getFacetSortValue(facetSort);
			parameters.append("&").append(facetSort).append("=").append(sortValue);
		}
		Logger.info(parameters.toString());
		this.facetParameters = parameters.toString();
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
}
