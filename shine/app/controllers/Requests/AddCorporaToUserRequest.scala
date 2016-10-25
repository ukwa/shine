package controllers.Requests

import play.api.mvc.ActionTransformer

import scala.concurrent.Future
import scala.collection.JavaConverters._

/**
  * A transformer that will add the current users Corpora to the Request object. The user will be null if not set.
  * The Corpora can then be accessed as request.corpora.
  *
  * Can only be used on top of a UserRequest.
  */
object AddCorporaToUserRequest extends ActionTransformer[UserRequest, CorporaRequest] {
  def transform[A](request: UserRequest[A]) = Future.successful {
    new CorporaRequest(
      request.user,
      models.Corpus.findByUser(request.user).asScala.toList,
      request
    )
  }
}