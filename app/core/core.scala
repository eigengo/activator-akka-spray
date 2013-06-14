package core

import akka.actor.{Props, ActorSystem}

/**
 * This trait contains the main components of the application we are building.
 */
trait Core {
  def system: ActorSystem
}

/**
 * This trait implements ``Core`` by starting the required ``ActorSystem``
 */
trait BootedCore extends Core {

  /**
   * Construct the ActorSystem we will use in our application
   */
  implicit val system = ActorSystem("akka-spray")

  /**
   * Ensure that the constructed ActorSystem is shut down when the JVM shuts down
   */
  system.registerOnTermination(system.shutdown())

}

/**
 * This trait contains the actors that make up our application
 */
trait CoreActors {
  this: Core =>

  val registration = system.actorOf(Props[RegistrationActor])

}