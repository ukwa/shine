package test

import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._

import org.fluentlenium.core.filter.FilterConstructor._

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
@RunWith(classOf[JUnitRunner])
class SearchPageSpec extends Specification {
  
	"Application" should {
    
		"run in a browser" in new WithBrowser {
			browser.goTo("/search")

			browser.url must equalTo("/search")

        	browser.$("title").first.getText must equalTo("Search")

        	browser.$("a").first.getText must equalTo("UK Web Archive")
        	
        	browser.$("a", withText("Search")).click()
        	
        	browser.$("h4.panel-title.pull-left.filter-heading-title").first.getText must equalTo("Results not found")
        	
        	browser.$("#query").text("Wikipedia")
			browser.$("#search").click()
			
        	browser.$("h4.panel-title.pull-left.filter-heading-title").first.getText must contain("Results 1 to ")
			
			browser.$("a#facet-sort-count span.label.label-primary").first.getText must equalTo("count")
			browser.$("a#facet-sort-value span.label.label-primary").first.getText must equalTo("value")
		}    
	}
}