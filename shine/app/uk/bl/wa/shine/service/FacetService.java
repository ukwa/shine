package uk.bl.wa.shine.service;

import java.util.Map;

import uk.bl.wa.shine.model.FacetValue;

/**
 * @author kli
 *
 */

public interface FacetService {
	Map<String, FacetValue> getFacetValues();
	Map<String, FacetValue> getAdditionalFacetValues();
	void addFacetValue(String key);
	void removeFacetValue(String key);

}
