package uk.bl.wa.shine.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;

import uk.bl.wa.shine.model.FacetValue;

/**
 * @author kli
 *
 */

public class FacetServiceImpl implements FacetService {

	private Map<String, List<FacetValue>> facetMap = null;
	private Map<String, List<FacetValue>> defaultFacetMap = null;
	private List<FacetValue> facetList = null;
	private List<FacetValue> defaultFacetList = null;

	public FacetServiceImpl() {
		this.facetMap = new HashMap<String, List<FacetValue>>();
		this.defaultFacetMap = new HashMap<String, List<FacetValue>>();
		for (String facetName : facetMap.keySet()) {
			List<FacetValue> facetList = facetMap.get(facetName);
			if (facetName.equals("basic")) {
				this.defaultFacetMap.put(facetName, facetList);
				this.defaultFacetList = facetList;
			}
			this.defaultFacetMap.put(facetName, facetList);
			this.facetMap.put(facetName, facetList);
			this.facetList = facetList;
		}
		Logger.info("facetMap: " + facetMap);
		Logger.info("defaultFacetMap: " + defaultFacetMap);
		Logger.info("facetList: " + facetList);
		Logger.info("defaultFacetList: " + defaultFacetList);
	}
	
	@Override
	public void add(String facetName, List<FacetValue> facetValues) {
		this.facetMap.put(facetName, facetValues);
	}

	@Override
	public Map<String, List<FacetValue>> getMap() {
		return this.facetMap;
	}

	@Override
	public Map<String, List<FacetValue>> getDefaultMap() {
		return this.defaultFacetMap;
	}

	@Override
	public List<FacetValue> getList() {
		return this.facetList;
	}

	@Override
	public List<FacetValue> getDefaultList() {
		return this.defaultFacetList;
	}

}
