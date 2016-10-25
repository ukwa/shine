package controllers

import controllers.Requests.Actions
import com.google.inject.Inject
import com.google.inject.name.Named
import com.google.inject.Singleton
import models._
import play.api.data.Forms._
import play.api.data._
import play.api.mvc._
import uk.bl.wa.shine._
import views._
import play.api.routing.JavaScriptReverseRouter

@Singleton
class Application @Inject() (implicit @Named("ShineConfiguration") shineConfig: play.api.Configuration) extends Controller {

  def index = Actions.UserAction { implicit request =>
    Ok(views.html.index("Welcome", request.user))
  }

  def searchTips = Actions.UserAction { implicit request =>
    Ok(views.html.search.searchTips("Search Tips", request.user))
  }

  // -- Authentication
  val loginForm = Form(
    tuple(
      "email" -> text,
      "password" -> text
    ) verifying("Invalid email or password", result => result match {
      case (email, password) => validate(email, password)
    })
  )

  /**
    * We only store lowercase emails and transform user input to lowercase for this field.
    *
    * @return null if authentication ok.
    */
  def validate(email: String, password: String) = {
    val user = User.findByEmail(email.toLowerCase())
    if (user != null) {
      val storedPassword = user.password
      val authenticate = PasswordHash.validatePassword(password, storedPassword)
      println("validating: " + authenticate)
      authenticate
    } else {
      false
    }
  }

  /**
    * Login page.
    */
  def login = Action { implicit request =>
    Ok(html.login(loginForm, "Login"))
  }

  /**
    * Handle login form submission.
    */
  def authenticate = Action { implicit request =>
    println("authenticate")
    loginForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.login(formWithErrors, "Login")),
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
    Ok(
      JavaScriptReverseRouter("jsRoutes")(
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
        routes.javascript.Search.ajaxSearch,
        routes.javascript.Account.saveSearch,
        routes.javascript.Account.saveCorpus,
        routes.javascript.Account.saveResources
      )).as("text/javascript")
  }
}
