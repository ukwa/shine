package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import views._
import org.apache.commons.lang.StringUtils
import uk.bl.wa.shine._
import scala.collection.JavaConverters

object Account extends Controller {

  case class PasswordData(currentPassword: String, newPassword1: String, newPassword2: String)
  
  val passwordForm = Form(
	  mapping(
	    "currentPassword" -> nonEmptyText,
	    "newPassword1" -> nonEmptyText,
	    "newPassword2" -> nonEmptyText
	  )(PasswordData.apply)(PasswordData.unapply)
  )

  def index = Action { implicit request =>
	  request.session.get("username").map { username =>
		val user = User.findByEmail(username.toLowerCase())
	    Ok(views.html.index("Shine Application", user))
	  }.getOrElse {
	    Ok(views.html.index("Shine Application", null))
	  }
  }

  def validatePassword(email: String, currentPassword: String, newPassword1: String, newPassword2: String) = {
    println("validatePassword")
	val storedPassword = User.findByEmail(email.toLowerCase()).password;
    val authenticate = PasswordHash.validatePassword(currentPassword, storedPassword);
    if (authenticate) {
    	(newPassword1.equals(newPassword2))
    } else {
      false
    }
  }

  def changePassword = Action { implicit request =>
  	  request.session.get("username").map { username =>
		val user = User.findByEmail(username.toLowerCase())
		Ok(html.changePassword(passwordForm, "Shine Application", user))
	  }.getOrElse {
		Unauthorized("Oops, you are not authorized")
	  }
  }
  
  def updatePassword = Action { implicit request =>
    println("updatePassword")
  	  request.session.get("username").map { username =>
		var user = User.findByEmail(username.toLowerCase())
		passwordForm.bindFromRequest.fold(
	      formWithErrors => {
	        BadRequest(html.changePassword(formWithErrors, "Shine Application", user))
	      },
	      passwordData => {
	        val password = passwordData.currentPassword
	        val newPassword1 = passwordData.newPassword1
	        val newPassword2 = passwordData.newPassword2
	    	val storedPassword = user.password
		    val authenticate = PasswordHash.validatePassword(password, storedPassword)
		    if (authenticate) {
		    	if (newPassword1.equals(newPassword2)) {
		    		// save password
		    		user = User.update(username, newPassword1)
		    		Redirect(routes.Application.index).flashing("success" -> "Your password has been updated successfully")
		    	} else {
		    		//passwordForm.fill(passwordData).withGlobalError("Your error message")
	                BadRequest(html.changePassword(passwordForm.fill(passwordData).withGlobalError("New Passwords do not match"), "Shine Application", user))	    	
	            }
		    } else {
	            BadRequest(html.changePassword(passwordForm.fill(passwordData).withGlobalError("Current Password is incorrect"), "Shine Application", user))	    	
		    }
	      })
  	  }.getOrElse {
		Unauthorized("Oops, you are not authorized")
	  }
  }
  
  def mySearches = Action { implicit request =>
  	  request.session.get("username").map { username =>
		val user = User.findByEmail(username.toLowerCase())
		val searches = models.Search.findByUser(user)
		val ls = JavaConverters.asScalaBufferConverter(searches).asScala.toList
	    Ok(views.html.mySearches("My Searches", user, ls))
	  }.getOrElse {
		Unauthorized("Oops, you are not authorized")
	  }
  }

  def saveSearch(name: String, url: String) = Action { implicit request =>
  	  request.session.get("username").map { username =>
		val user = User.findByEmail(username.toLowerCase())
		// insert stuff
	    //Ok(views.html.mySearches("My Searches", user))
		// redirect back to search you just saved.
		val search = models.Search.create(name, url, user.uid)
		println("saved search: " + search.name + " - " + search.url + " - " + search.user_id)
		Ok("false")
	    //Redirect(routes.Account.mySearches).flashing("success" -> "Search was added")
	  }.getOrElse {
		Unauthorized("Oops, you are not authorized")
	  }
  }

}
