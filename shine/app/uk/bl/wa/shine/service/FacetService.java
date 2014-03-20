package uk.bl.wa.shine.service;

import java.util.List;
import java.util.Map;

import uk.bl.wa.shine.model.FacetValue;

/**
 * @author kli
 *
 */

public interface FacetService {
	void add(String facetName, List<FacetValue> facetValues);
	Map<String, List<FacetValue>> getMap();
	Map<String, List<FacetValue>> getDefaultMap();
	List<FacetValue> getList();
	List<FacetValue> getDefaultList();
}
