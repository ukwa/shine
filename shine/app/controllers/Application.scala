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
