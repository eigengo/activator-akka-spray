package api

import core.{CoreActors, Core}
import akka.actor.Props

/**!
 * The REST API layer. It exposes the REST services, but does not provide any
 * web server interface.<br/>
 * Notice that it requires to be mixed in with ``core.Core``, which provides access
 * to the top-level actors that make up the system.
 */
trait Api {
  this: CoreActors with Core =>

  private implicit val _ = system.dispatcher

  val routes = new RegistrationService(registration).route

  val rootService = system.actorOf(Props(new RoutedHttpService(routes)))

}
