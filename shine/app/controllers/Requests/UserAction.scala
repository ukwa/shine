package controllers.Requests

import models.User
import play.api.mvc.{Request, _}

import scala.concurrent.Future

/**
  * An action that will include the current user. The user will be null if not set.
  * Use this if you need the user in the view, but don't care if the user is authenticated.
  */
object UserAction extends ActionBuilder[UserRequest] {
  def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] = {
    request.session.get("username")
      .map(User.findByEmail)
      .map(user => block(new UserRequest(user, request)))
      .getOrElse(block(new UserRequest(null, request)))
  }
}