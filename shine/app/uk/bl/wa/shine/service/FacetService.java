package uk.bl.wa.shine.service;

import java.util.Map;

import uk.bl.wa.shine.model.FacetValue;

/**
 * @author kli
 *
 */

public interface FacetService {
	Map<String, FacetValue> getSelected();
	Map<String, FacetValue> getDefaults();
	Map<String, FacetValue> getOptionals();
	Map<String, FacetValue> getAll();
	void add(String key);
	void remove(String key);
	void reset();
}
