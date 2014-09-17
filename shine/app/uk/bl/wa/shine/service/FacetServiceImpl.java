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

	// for you current list of selected facets
	private Map<String, FacetValue> selectedFacets = null;
	// original default facets
	private Map<String, FacetValue> defaults = null;
	// for your dropdown
	private Map<String, FacetValue> optionals = null;

	private Map<String, FacetValue> defaultOptionals = null;
	
	public FacetServiceImpl(Configuration configuration) {
		init();
		if (configuration != null) {

			Map<String, Object> map = configuration.getConfig("facets").asMap();

			for (String facetHeader : map.keySet()) {
				Logger.info("facetHeader: " + facetHeader); // i.e additions
				// i.e. content_language { name="Language", limit="5", maxlimit="10" },
				@SuppressWarnings("unchecked")
				Map<String, Map<String,Object>> values = (Map<String, Map<String,Object>>) map.get(facetHeader);
				for (String key : values.keySet()) {
					Map<String,Object> facet = values.get(key);
					String name = facet.get("name").toString();
					Integer limit = Integer.valueOf(facet.get("limit").toString());
					Integer maxLimit = Integer.valueOf(facet.get("maxLimit").toString());
					Logger.info(key + " { " + name + ", " + limit + ", " + maxLimit + "}");
					FacetValue facetValue = new FacetValue(key, name, limit, 10);
					Logger.info("facetValue: " + facetValue.getName() + "=" + facetValue.getValue());
					// just load the basic (default) ones first
					if (facetHeader.equals("basic")) {
						this.defaults.put(key, facetValue);
					} else {
						this.optionals.put(key, facetValue);
					}
				}
			}
		}
		this.selectedFacets = new HashMap<String, FacetValue>(this.defaults);
		this.defaultOptionals = new HashMap<String, FacetValue>(this.optionals);
	}

	public void init() {
		this.defaults = new HashMap<String, FacetValue>();
		this.optionals = new HashMap<String, FacetValue>();
	}

	@Override
	public Map<String, FacetValue> getSelected() {
		return this.selectedFacets;
	}

	@Override
	public Map<String, FacetValue> getOptionals() {
		return this.optionals;
	}

	@Override
	public void add(String key) {
		// get the facet value based on key
		FacetValue facetValue = this.optionals.get(key);
		selectedFacets.put(key, facetValue);
		optionals.remove(key);
	}

	@Override
	public void remove(String key) {
		if (!this.defaults.containsKey(key)) {
			FacetValue facetValue = this.selectedFacets.get(key);
			this.selectedFacets.remove(key);
			Logger.info("facetValue found for removing: " + facetValue);
			this.optionals.put(key, facetValue);
		}
	}

	@Override
	public Map<String, FacetValue> getDefaults() {
		return this.defaults;
	}

	@Override
	public void reset() {
		this.selectedFacets = new HashMap<String, FacetValue>(this.defaults);
		this.optionals = new HashMap<String, FacetValue>(this.defaultOptionals);
	}

	@Override
	public Map<String, FacetValue> getAll() {
        Map<String, FacetValue> retval = new HashMap<String, FacetValue>();
        retval.putAll(this.defaults);
        retval.putAll(this.defaultOptionals);
        return retval;
	}		
}
