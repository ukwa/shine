import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import scala.concurrent.Future
import models._

object Global extends GlobalSettings {

  override def onError(request: RequestHeader, ex: Throwable) = {
    Future.successful(InternalServerError(
      views.html.errorPage(ex)
    ))
  }
  
  override def onStart(app: Application) {
    InitialData.insert()
  }

}

object InitialData {
  
  def insert() = {
    
    if(User.findAll.isEmpty) {
    	User.create("kinman.li@bl.uk", "secret")
    }
    
  }
  

}