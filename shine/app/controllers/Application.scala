package controllers

import play.api._
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models._
import views._
import org.apache.commons.lang.StringUtils
import uk.bl.wa.shine._

object Application extends Controller {

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

  // -- Authentication

  val loginForm = Form(
    tuple(
      "email" -> text,
      "password" -> text
    ) verifying ("Invalid email or password", result => result match {
      case (email, password) => validate(email, password)
    })
  )

  /**
   * We only store lowercase emails and transform user input to lowercase for this field.
   * @return null if authentication ok.
   */
  
  def validate(email: String, password: String) = {
    val user = User.findByEmail(email.toLowerCase())
    if (user != null) {
		val storedPassword = user.password
	    val authenticate = PasswordHash.validatePassword(password, storedPassword)
		//val authenticate = testUser(email, password) == true
		println("validating: " + authenticate)
		authenticate
    } else {
    	false
    }
  }
  
//  val passwordForm = Form(
//    tuple(
//      "email" -> text,
//      "currentPassword" -> text,
//      "newPassword1" -> text,
//      "newPassword2" -> text
//    ) verifying ("Missing Credentials - Current Password, New Passwords do not match", result => result match {
//      case (email, currentPassword, newPassword1, newPassword2) => validatePassword(email, currentPassword, newPassword1, newPassword2)
//    })
//  )

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
  
  def testUser(username: String, password: String) = {
    (username == "admin@test.com" && password == "secret")  
  }
  
  /**
   * Login page.
   */
  def login = Action { implicit request =>
    Ok(html.login(loginForm, "Shine Application"))
  }

  /**
   * Handle login form submission.
   */
  def authenticate = Action { implicit request =>
    println("authenticate")
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors, "Shine Application")),
      account => Redirect(routes.Application.index).withSession("username" -> account._1)
    )
  }

  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(routes.Application.login).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }
  def changePassword(email: String) = Action { implicit request =>
	val user = User.findByEmail(email.toLowerCase())
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

  // -- Javascript routing

  def javascriptRoutes = Action { implicit request =>
    import routes.javascript._
    Ok(
      Routes.javascriptRouter("jsRoutes")(
        routes.javascript.Search.suggestTitle,
        routes.javascript.Search.suggestUrl,
        routes.javascript.Search.suggestFileFormat,
        routes.javascript.Search.suggestLinksHosts,
        routes.javascript.Search.suggestLinksDomains,
        routes.javascript.Search.suggestLinksPublicSuffixes,
        routes.javascript.Search.suggestAuthor,
        routes.javascript.Search.suggestCollection,
        routes.javascript.Search.suggestCollections,
        routes.javascript.Search.getFacets,
        routes.javascript.Search.processChart,
        routes.javascript.Search.ajaxSearch
        )).as("text/javascript")
  }
}

/**
 * Provide security features
 */
trait Secured {
  
  /**
   * Retrieve the connected user email.
   */
  private def username(request: RequestHeader) = request.session.get("email")

  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.login)
  
  // --
  
  /** 
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) { user =>
    Action(request => f(user)(request))
  }
}
