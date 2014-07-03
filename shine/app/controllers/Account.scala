package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import views._
import org.apache.commons.lang.StringUtils
import uk.bl.wa.shine._

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

  def changePassword(email: String) = Action { implicit request =>
	val user = User.findByEmail(email.toLowerCase())
	println("user: " + user)
	Ok(html.changePassword(passwordForm, "Shine Application", user))
  }
  
  def updatePassword(email: String) = Action { implicit request =>
    println("updatePassword")
	var user = User.findByEmail(email.toLowerCase())
    passwordForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(html.changePassword(formWithErrors, "Shine Application", user))
      },
      passwordData => {
        val password = passwordData.currentPassword
        val newPassword1 = passwordData.newPassword1
        val newPassword2 = passwordData.newPassword2
    	val storedPassword = user.password
    	println(password + " " + newPassword1 + " " + newPassword2)
	    val authenticate = PasswordHash.validatePassword(password, storedPassword)
	    if (authenticate) {
	      println("authenticate: " + authenticate)
	    	if (newPassword1.equals(newPassword2)) {
	    		// save password
	    		user = User.update(email, newPassword1)
	    		Redirect(routes.Application.index).flashing("success" -> "Your password has been updated successfully")
	    	} else {
	    		//passwordForm.fill(passwordData).withGlobalError("Your error message")
                BadRequest(html.changePassword(passwordForm.fill(passwordData).withGlobalError("New Passwords do not match"), "Shine Application", user))	    	
            }
	    } else {
            BadRequest(html.changePassword(passwordForm.fill(passwordData).withGlobalError("Current Password is incorrect"), "Shine Application", user))	    	
	    }
      })
  }
  
  def mySearches = Action { implicit request =>
  	  request.session.get("username").map { username =>
		val user = User.findByEmail(username.toLowerCase())
	    Ok(views.html.mySearches("My Searches", user))
	  }.getOrElse {
		Unauthorized("Oops, you are not authorized")
	  }
  }
}
