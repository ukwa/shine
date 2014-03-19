package uk.bl.wa.shine.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.bl.wa.shine.model.FacetValue;

/**
 * @author kli
 *
 */

public class FacetServiceImpl implements FacetService {

	private Map<String, List<FacetValue>> facetList = new HashMap<String, List<FacetValue>>();
	private Map<String, List<FacetValue>> defaultFacetList = new HashMap<String, List<FacetValue>>();
	
	@Override
	public void add(String facetName, List<FacetValue> facetValues) {
		this.facetList.put(facetName, facetValues);
	}

	@Override
	public Map<String, List<FacetValue>> getList() {
		return this.facetList;
	}

	@Override
	public Map<String, List<FacetValue>> getDefaultList() {
		return this.defaultFacetList;
	}

}
