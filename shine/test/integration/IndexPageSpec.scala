package integration

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
class IndexPageSpec extends Specification {
  
	"Application" should {
    
		"run in a browser" in new WithBrowser {
			browser.goTo("/")
        	browser.$("h1").first.getText must contain("Welcome")
        	browser.$("a").first.getText must equalTo("UK Web Archive")

//        	browser.$("a", withText("Search")).click()
        	
        	//browser.$("div.collapse.navbar-collapse ul.nav.navbar-nav li a", withText("Search")).click()
        	
//			browser.$("div.collapse.navbar-collapse ul.nav.navbar-nav li a").getTexts().get(1) must equalTo("Trends")

//			browser.$("a").getTexts().get(1) must equalTo("Half-life")
    	
//			browser.url must equalTo("/search")
		}    
	}
}