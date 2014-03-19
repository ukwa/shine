package uk.bl.wa.shine.bdd.stories;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;

import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;

/**
 * @author kli
 *
 */

public class AddFacetSteps {

	private List<String> facetList;

	@Given("a list of current facets")
	public void givenCurrentFacetsList() {
		facetList = new ArrayList<String>();
		// existing facet in list
		facetList.add("crawl_year");
	}

	@When("I add a new <facet>")
	public void whenIAddFacet(@Named("facet") String facet) {
		facetList.add(facet);
	}

	@Then("the outcome should show <result> with new facet")
	public void thenTheOutcomeShould(@Named("result") List<String> result) {
		Assert.assertEquals(2, result.size());
		Assert.assertEquals(facetList.size(), result.size());
	}
}
