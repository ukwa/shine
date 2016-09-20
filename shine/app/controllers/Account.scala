package controllers

import models._
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.{JsNumber, JsString, Json}
import play.api.mvc._
import javax.inject._

import controllers.Requests.{Actions}
import play.api.i18n.{I18nSupport, MessagesApi}
import uk.bl.wa.shine._
import utils.Formatter
import views._

import scala.collection.JavaConverters._

case class PasswordData(currentPassword: String, newPassword1: String, newPassword2: String)

@Singleton
class Account @Inject() (implicit val messagesApi: MessagesApi, @Named("ShineConfiguration") shineConfig: play.api.Configuration) extends Controller with I18nSupport {

  val passwordForm = Form(
    mapping(
      "currentPassword" -> nonEmptyText,
      "newPassword1" -> nonEmptyText,
      "newPassword2" -> nonEmptyText
    )(PasswordData.apply)(PasswordData.unapply)
  )

  def changePassword = Actions.AuthenticatedUserAction { implicit request =>
    Ok(html.account.changePassword(passwordForm, "Change password", request.user))
  }

  def updatePassword() = Actions.AuthenticatedUserAction { implicit request =>
    println("updatePassword")
    val user = request.user

    passwordForm.bindFromRequest.fold(
      formWithErrors => {
        BadRequest(html.account.changePassword(formWithErrors, "Change password", user))
      },
      passwordData => {
        if (PasswordHash.validatePassword(passwordData.currentPassword, user.password)) {
          if (passwordData.newPassword1.equals(passwordData.newPassword2)) {
            // save password
            User.updatePassword(user.email, passwordData.newPassword1)
            Redirect(routes.Application.index()).flashing("success" -> "Your password has been updated successfully")
          } else {
            BadRequest(html.account.changePassword(passwordForm.fill(passwordData).withGlobalError("New Passwords do not match"), "Change password", user))
          }
        } else {
          BadRequest(html.account.changePassword(passwordForm.fill(passwordData).withGlobalError("Current Password is incorrect"), "Change password", user))
        }
      }
    )
  }

  def mySearches = Actions.AuthenticatedUserAction { implicit request =>
    Ok(views.html.account.mySearches(
      "My Searches",
      request.user,
      models.Search.findByUser(request.user).asScala.toList
    ))
  }

  def saveSearch(name: String, description: String, summary: String, url: String) = Actions.AuthenticatedUserAction { implicit request =>
      val user = request.user
      // insert stuff
      //Ok(views.html.mySearches("My Searches", user))
      // redirect back to search you just saved.
      println("summary: " + summary)
      val newSummary = summary.replace("<li>", " ").replace("</li><li>", " ").replace("</li>", " ").trim()
      println("newSummary: " + newSummary)
      val search = models.Search.create(name, description, newSummary, url, user.id)
      println("saved search: " + search.name + " - " + search.summary + " - " + search.url + " - " + search.user_id)
      Ok("false")
      //Redirect(routes.Account.mySearches).flashing("success" -> "Search was added")
  }

  // TODO: Validate that the search is owned by the user.
  def deleteSearch(id: Long) = Actions.AuthenticatedAction { implicit request =>
    models.Search.find(id).delete()
    Redirect(routes.Account.mySearches()).flashing("success" -> "Your search has been deleted")
  }

  def myCorpora = Actions.AuthenticatedUserAction { implicit request =>
    Ok(views.html.account.myCorpora(
      "My Corpora",
      request.user,
      models.Corpus.findByUser(request.user).asScala.toList
    ))
  }

  def saveCorpus(name: String, description: String) = Actions.AuthenticatedUserAction { implicit request =>
      models.Corpus.create(name, description, request.user.id)
      val myCorpora = models.Corpus.findByUser(request.user)

      var results = Json.arr()

      for (c <- myCorpora.asScala) {
        println("c: " + c.name)
        val json = Json.obj("id" -> JsNumber(c.id.longValue()), "name" -> JsString(c.name))
        results = results :+ json
      }

      Ok(results)
      //Redirect(routes.Account.mySearches).flashing("success" -> "Search was added")
  }

  // TODO: Validate that the search is owned by the user.
  def deleteCorpus(id: Long) = Actions.AuthenticatedAction { implicit request =>
    models.Corpus.find(id).delete()
    Redirect(routes.Account.myCorpora()).flashing("success" -> "Your corpus has been deleted")
  }

  // TODO: This should verify ownership of the corpus.
  def saveResources(id: String, resources: String) = Actions.AuthenticatedAction { implicit request =>
    val corpus = models.Corpus.find(id.toLong)
    println("Corpus found: " + corpus)
    val res = resources.split(",,,,,")

    for (resource <- res) {
      val r = resource.split(";;;")
      val id = r(0)
      val title = r(2)
      val url = r(4)
      val wayback = r(5)
      val waybackDate = Formatter.getDate(wayback)
      println("id: " + id + " " + title + " " + waybackDate)
      val res = new models.Resource(title, url, id, waybackDate)
      res.corpus = corpus
      res.save()
    }
    corpus.save()
    Ok("false")
    //Redirect(routes.Account.mySearches).flashing("success" -> "Search was added")
  }

  // TODO: Verify ownership of resource.
  def deleteResource(id: Long) = Actions.AuthenticatedAction { implicit request =>
    models.Resource.find(id).delete()
    Redirect(routes.Account.myCorpora()).flashing("success" -> "Your resource has been deleted")
  }
}
