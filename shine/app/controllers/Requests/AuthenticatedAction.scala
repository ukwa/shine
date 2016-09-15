package controllers.Requests

import play.api.mvc.{Request}
import scala.concurrent.Future
import play.api.mvc._
import models.User

/**
  * An action that will continue if the user is authenticated with a UserRequest, or send Forbidden.
  */
object AuthenticatedAction extends ActionBuilder[UserRequest] {
  def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] = {
    request.session.get("username")
      .map(User.findByEmail)
      .map(user => block(new UserRequest(user, request)))
      .getOrElse(Future.successful(Results.Forbidden("Oops, you are not authorized")))
  }
}