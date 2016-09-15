package controllers.Requests

import play.api.mvc.{Request, WrappedRequest}
import models.User

// A wrapped request that includes the current user. Can be accessed as req.user.
class UserRequest[A](val user: User, val request: Request[A]) extends WrappedRequest[A](request)