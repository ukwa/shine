import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._
import uk.bl.wa.shine.service.FacetServiceImpl

@RunWith(classOf[JUnitRunner])
class FacetValueModelSpec extends Specification {
  
  import uk.bl.wa.shine.model.FacetValue

    "be listed along its facet values" in {
      running(FakeApplication(additionalConfiguration = inMemoryDatabase())) {
//        var facetService = new FacetServiceImpl(config)
//        val facetValues = Computer.list()
//
//        facetValues.size must equalTo(574)
//
      }
    }
  
}