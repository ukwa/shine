package controllers.Requests

object Actions {
  val AuthenticatedAction = AuthenticatedOnly
  val AuthenticatedUserAction = AuthenticatedOnly andThen AddUserToRequest
  val UserAction = AddUserToRequest
}
