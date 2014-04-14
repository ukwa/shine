package model;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import uk.bl.wa.shine.model.FacetValue;
import uk.bl.wa.shine.service.FacetService;
import uk.bl.wa.shine.service.FacetServiceImpl;

public class FacetValueTest {

	private Map<String, Map<String, FacetValue>> facetMap = null;
	private Map<String, FacetValue> facetValues = null;
	private FacetService facetService = null;
	
	@Before
	public void setUp() throws Exception {
		this.facetMap = new HashMap<String, Map<String, FacetValue>>();
		this.facetValues = new HashMap<String, FacetValue>();
		FacetValue facetValue = new FacetValue("domain", "Domain");
		// TODO: add some more facets
		this.facetValues.put(facetValue.getName(), facetValue);
		this.facetMap.put("basic", this.facetValues);
		facetService = new FacetServiceImpl(facetMap);

	}

	@Test
	public void testFacetValueSize() {
		Assert.assertEquals(1, facetService.getSelected().size());
	}

}
