package controllers.Requests

object Actions {
  // Only allows authenticated users.
  val AuthenticatedAction = AuthenticatedOnly

  // Adds the user to the request as request.user. The user is null if not authenticated.
  val UserAction = AddUserToRequest

  // Only allows authenticated users, and adds the user to the request object.
  val AuthenticatedUserAction = AuthenticatedAction andThen UserAction

  // UserWithCorporaAction
  val UserWithCorporaAction = AuthenticatedUserAction andThen AddCorporaToUserRequest
}
