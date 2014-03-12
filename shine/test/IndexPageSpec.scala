package test

import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

/**
 * add your integration spec here.
 * An integration test will fire up a whole play application in a real (or headless) browser
 */
class IndexPageSpec extends Specification {
  
	"Application" should {
    
		"run in a browser" in new WithBrowser {
			browser.goTo("/")

			browser.$("title").getTexts().get(0) must equalTo("Shine Application")

//			browser.$("a").click()
    	
//			browser.url must equalTo("/search")
		}    
	}
}