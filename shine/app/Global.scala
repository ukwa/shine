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
    	
    	val create = new Permission("Create", "Create")
    	create.save()
    	val read = new Permission("Read", "Read")
    	read.save()
    	val update = new Permission("Update", "Update")
    	update.save()
    	val delete = new Permission("Delete", "Delete")
    	delete.save()
    	
    	val role = new Role("Admin", "Admin")
    	role.save()

    	role.permissions.add(create)
    	role.permissions.add(read)
    	role.permissions.add(update)
    	role.permissions.add(delete)
    	
    	val user = User.create("kinman.li@bl.uk", "secret")
    	user.save()
    }
    
  }
  

}