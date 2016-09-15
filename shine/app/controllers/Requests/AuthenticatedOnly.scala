package controllers.Requests

import play.api.mvc.{Request}
import scala.concurrent.Future
import play.api.mvc._
import models.User

/**
  * An ActionFilter that will continue if the user is authenticated or send Forbidden.
  */
object AuthenticatedOnly extends ActionBuilder[Request] with ActionFilter[Request] {
  def filter[A](request: Request[A]): Future[Option[Result]] = {
    val result = request.session.get("username").map(User.findByEmail) match {
      case Some(username) => None
      case None => Some(Results.Forbidden("Oops, you are not authorized"))
    }

    Future.successful(result)
  }
}