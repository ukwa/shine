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

    	role.permissions.add(create)
    	role.permissions.add(read)
    	role.permissions.add(update)
    	role.permissions.add(delete)

    	role.save()
    	
    	val user = User.create("kinman.li@bl.uk", "secret")
    	user.roles.add(role)
    	user.save()
    	getExternalUsers()
    	
    }
    
  }
  
  def getExternalUsers() {

    val read = Permission.findByName("Read")
    val role = new Role("Reader", "Reader")
	role.permissions.add(read)
	role.save()
	
	var user = User.create("alison.kay@northumbria.ac.uk", "secret")
	user.roles.add(role)
	user.save()
	
	user = User.create("r.cran@alumni.ucl.ac.uk", "secret")
	user.roles.add(role)
	user.save()
	
	user = User.create("fryerc@parliament.uk", "secret")
	user.roles.add(role)
	user.save()
	
	user = User.create("Gareth.Millward@lshtm.ac.uk", "secret")
	user.roles.add(role)
	user.save()
	
	user = User.create("H.Raffal@2006.hull.ac.uk", "secret")
	user.roles.add(role)
	user.save()
	
	user = User.create("helenlouisetaylor@gmail.com", "secret")
	user.roles.add(role)
	user.save()
	
	user = User.create("mm2015@cam.ac.uk", "secret")
	user.roles.add(role)
	user.save()
	
	user = User.create("R.Deswarte@uea.ac.uk", "secret")
	user.roles.add(role)
	user.save()
	
	user = User.create("l.richardson@ucl.ac.uk", "secret")
	user.roles.add(role)
	user.save()

	user = User.create("daust01@mail.bbk.ac.uk", "secret")
	user.roles.add(role)
	user.save()

	user = User.create("S.V.Huc-Hepher@westminster.ac.uk", "secret")
	user.roles.add(role)
	user.save()
  }
  
  

}