package controllers.Requests

import models.User
import play.api.mvc.{Request, _}

import scala.concurrent.Future

/**
  * An action that will add the current user to the Request object. The user will be null if not set.
  * The User can then be accessed as request.User.
  *
  * Note: Does not in any way authenticate the user.
  */
object AddUserToRequest extends ActionBuilder[UserRequest] {
  def invokeBlock[A](request: Request[A], block: UserRequest[A] => Future[Result]): Future[Result] = {
    request.session.get("username")
      .map(User.findByEmail)
      .map(user => block(new UserRequest(user, request)))
      .getOrElse(block(new UserRequest(null, request)))
  }
}