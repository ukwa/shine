import static org.fest.assertions.Assertions.assertThat;
import cucumber.api.PendingException;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import play.Logger;
import play.test.TestBrowser;
import static org.fluentlenium.core.filter.FilterConstructor.*;

public class HomePageSteps {
	
	private TestBrowser browser = null;
	private String url = null;
	
	@Given("^I am on the Home Page$")
	public void i_am_on_the_Home_Page() throws Throwable {
		browser = GlobalHooks.TEST_BROWSER;
		url = "http://localhost:" + GlobalHooks.PORT;
	}

	@When("^the home page loads$")
	public void the_home_page_loads() throws Throwable {
		Logger.debug("url: " + url);
        browser.goTo(url);
	}

	@Then("^I should see home page title \"(.*?)\"$")
	public void i_should_see_home_page_title(String title) throws Throwable {
        assertThat(browser.title()).isEqualTo(title);
	}

	@Then("^first menu option is \"(.*?)\"$")
	public void first_menu_option_is(String name) throws Throwable {
		Logger.debug("find: " + browser.find("#nav-search").getName());
        assertThat(browser.pageSource().contains(name));
	}

	@Then("^second menu option is \"(.*?)\"$")
	public void second_menu_option_is(String name) throws Throwable {
        assertThat(browser.pageSource().contains(name));
	}

}
