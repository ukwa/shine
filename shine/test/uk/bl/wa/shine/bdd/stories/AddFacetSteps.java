package uk.bl.wa.shine.bdd.stories;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.jbehave.core.annotations.AsParameterConverter;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Pending;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

import uk.bl.wa.shine.model.FacetValue;
import uk.bl.wa.shine.service.FacetService;
import uk.bl.wa.shine.service.FacetServiceImpl;

/**
 * @author kli
 *
 */

public class AddFacetSteps {

	private FacetService facetService;

	@Given("a facet service")
	public void givenCurrentFacetsList() {
		facetService = new FacetServiceImpl();
		// existing facet in list
		List<FacetValue> facetValues = new ArrayList<FacetValue>();
		facetValues.add(new FacetValue("crawl_year", "Crawl Year"));
		facetService.add("basic", facetValues);
	}

	@When("I add a new <facet>")
	public void whenIAddFacet(@Named("facet") String facet) {
		List<FacetValue> facetValues = new ArrayList<FacetValue>();
		facetValues.add(new FacetValue("domain", "Domain"));
		facetService.add("additional", facetValues);
	}

	@Then("the outcome should show <result> with added facet $result")
	public void thenTheOutcomeShould(@Named("result") Map<String, List<FacetValue>> result) {
		Assert.assertEquals(2, result.size());
		Assert.assertEquals(facetService.getList().size(), result.size());
	}
	
    @AsParameterConverter
    public Map<String, List<FacetValue>> createFacet(String name) {
    	Map<String, List<FacetValue>> values = new HashMap<String, List<FacetValue>>();
		List<FacetValue> facetValues = new ArrayList<FacetValue>();
        facetValues.add(new FacetValue("domain", "Domain"));
    	values.put("additional", facetValues);
		return values;
    }
}
