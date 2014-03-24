package uk.bl.wa.shine.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import play.Configuration;
import play.Logger;

import uk.bl.wa.shine.model.FacetValue;

/**
 * @author kli
 * 
 */

public class FacetServiceImpl implements FacetService {

	// for you current list of selected facets
	private Map<String, FacetValue> selectedFacetValues = null;
	// original default facets
	private Map<String, FacetValue> defaultFacetValues = null;
	// for your dropdown
	private Map<String, FacetValue> additionalFacetValues = null;

	
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
					this.selectedFacetValues.put(key, facetValue);
					this.defaultFacetValues.put(key, facetValue);
				} else {
					this.additionalFacetValues.put(key, facetValue);
				}
			}
		}
		Logger.info("facetValues: " + selectedFacetValues);
	}

	public void init() {
		this.selectedFacetValues = new HashMap<String, FacetValue>();
		this.defaultFacetValues = new HashMap<String, FacetValue>();
		this.additionalFacetValues = new HashMap<String, FacetValue>();
	}

	@Override
	public Map<String, FacetValue> getSelectedFacetValues() {
		return this.selectedFacetValues;
	}

	@Override
	public Map<String, FacetValue> getAdditionalFacetValues() {
		return this.additionalFacetValues;
	}

	@Override
	public void addFacetValue(String key) {
		// get the facet value based on key
		FacetValue facetValue = this.additionalFacetValues.get(key);
		selectedFacetValues.put(key, facetValue);
		additionalFacetValues.remove(key);
	}

	@Override
	public void removeFacetValue(String key) {
		if (!this.defaultFacetValues.containsKey(key)) {
			FacetValue facetValue = this.selectedFacetValues.get(key);
			selectedFacetValues.remove(key);
			Logger.info("facetValue found for removing: " + facetValue);
			additionalFacetValues.put(key, facetValue);
		}
	}

	@Override
	public Map<String, FacetValue> getDefaultFacetValues() {
		return this.defaultFacetValues;
	}

	@Override
	public void resetFacets() {
		// put it all back in additionalFacetValues
		Logger.info("sizes: " + defaultFacetValues.size() + "/" + additionalFacetValues.size() + "/" + selectedFacetValues.size());
//		for (Entry<String, FacetValue> defaultFacet : defaultFacetValues.entrySet()) {
//			selectedFacetValues = new HashMap<String, FacetValue>();
//			Logger.info("defaultFacet.getKey(): " + defaultFacet.getKey() + "/" + defaultFacet.getValue());
//			selectedFacetValues.put(defaultFacet.getKey(), defaultFacet.getValue());
//		}
//		Logger.info("selectedFacetValues: " + selectedFacetValues);
//		for (Entry<String, FacetValue> selectedFacet : selectedFacetValues.entrySet()) {
//			String key = selectedFacet.getKey();
//			if (!additionalFacetValues.containsKey(key) && !defaultFacetValues.containsKey(key)) {
//				additionalFacetValues.put(key, selectedFacetValues.get(key));
//			}
//		}
	}		
}
