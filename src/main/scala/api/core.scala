package core

import akka.actor.{Props, ActorRefFactory, ActorSystem}
import akka.io.IO
import api.{RoutedHttpService, Api}
import spray.can.Http
import web.StaticResources

/**
 * Core is type containing the ``system: ActorSystem`` member. This enables us to use it in our
 * apps as well as in our tests.
 */
trait Core {

  protected implicit def system: ActorSystem

}

/**
 * This trait implements ``Core`` by starting the required ``ActorSystem`` and registering the
 * termination handler to stop the system when the JVM exits.
 */
trait BootedCore extends Core with Api with StaticResources {
  def system: ActorSystem = ActorSystem("activator-akka-spray")
  def actorRefFactory: ActorRefFactory = system
  val rootService = system.actorOf(Props(new RoutedHttpService(routes ~ staticResources )))

  IO(Http)(system) ! Http.Bind(rootService, "0.0.0.0", port = 8080)

  /**
   * Construct the ActorSystem we will use in our application
   */
  //protected implicit  val system : ActorSystem

  /**
   * Ensure that the constructed ActorSystem is shut down when the JVM shuts down
   */
  sys.addShutdownHook(system.shutdown())

}

/**
 * This trait contains the actors that make up our application; it can be mixed in with
 * ``BootedCore`` for running code or ``TestKit`` for unit and integration tests.
 */
trait CoreActors {
  this: Core =>

  val registration = system.actorOf(Props[RegistrationActor])
  val messenger    = system.actorOf(Props[MessengerActor])
  val fruit = system.actorOf(Props[FruitActor])

}