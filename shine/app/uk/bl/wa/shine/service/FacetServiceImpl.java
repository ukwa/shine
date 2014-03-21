package uk.bl.wa.shine.service;

import java.util.HashMap;
import java.util.Map;

import play.Configuration;
import play.Logger;
import uk.bl.wa.shine.model.FacetValue;

/**
 * @author kli
 * 
 */

public class FacetServiceImpl implements FacetService {

	private Map<String, Map<String, FacetValue>> facetMap = null;
	private Map<String, FacetValue> facetValues = null;
	private Map<String, FacetValue> additionalFacetValues = null;
	
	public FacetServiceImpl(Map<String, Map<String, FacetValue>> facetMap) {
		this.facetMap = facetMap;
	}
	
	public FacetServiceImpl(Configuration configuration) {
		init();
		Map<String, Object> map = configuration.getConfig("facets").asMap();
		for (String facetHeader : map.keySet()) {
			@SuppressWarnings("unchecked")
			Map<String, String> values = (Map<String, String>) map.get(facetHeader);
			for (String key : values.keySet()) {
				String value = values.get(key);
				FacetValue facetValue = new FacetValue(key, value);
				Logger.info("facetValue: " + facetValue.getName() + "=" + facetValue.getValue());
				// just load the basic (default) ones first
				if (facetHeader.equals("basic")) {
					this.facetValues.put(key, facetValue);
				} else {
					this.additionalFacetValues.put(key, facetValue);
				}
			}
			this.add(facetHeader, facetValues);
		}
		Logger.info("facetMap: " + facetMap);
		Logger.info("facetValues: " + facetValues);
	}

	public void init() {
		this.facetMap = new HashMap<String, Map<String, FacetValue>>();
		this.facetValues = new HashMap<String, FacetValue>();
		this.additionalFacetValues = new HashMap<String, FacetValue>();
	}

	public void add(String facetName, Map<String, FacetValue> facetValues) {
		this.facetMap.put(facetName, facetValues);
	}

	@Override
	public Map<String, FacetValue> getFacetValues() {
		return this.facetValues;
	}

	@Override
	public Map<String, FacetValue> getAdditionalFacetValues() {
		return this.additionalFacetValues;
	}

	@Override
	public void addFacetValue(String key) {
		// get the facet value based on key
		FacetValue facetValue = this.additionalFacetValues.get(key);
		facetValues.put(key, facetValue);
		additionalFacetValues.remove(key);
	}

	@Override
	public void removeFacetValue(String key) {
		FacetValue facetValue = this.facetValues.get(key);
		facetValues.remove(key);
		Logger.info("facetValue found for removing: " + facetValue);
		additionalFacetValues.put(key, facetValue);
	}
}
