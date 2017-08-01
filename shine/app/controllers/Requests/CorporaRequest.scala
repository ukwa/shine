package controllers.Requests

import models.{Corpus, User}
import play.api.mvc.WrappedRequest

/**
  * A wrapped request that includes the current user and the users corpora. Can be accessed as req.user.
  */
class CorporaRequest[A](val user: User, val corpora: List[Corpus], val request: UserRequest[A]) extends WrappedRequest[A](request)