package integration

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
class AdvancedSearchPageSpec extends Specification {
  
	"Application" should {
    
		"run in a browser" in new WithBrowser {
			browser.goTo("/login")

			browser.url must equalTo("/login")

        	browser.$("title").first.getText must contain("Welcome")

        	browser.$("a").first.getText must equalTo("UK Web Archive")

		}    
	}
}