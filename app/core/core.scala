package core

import akka.actor.ActorSystem

/**
 * This trait contains the main components of the application we are building.
 */
trait Core {

  /**
   * Construct the ActorSystem we will use in our application
   */
  implicit val system = ActorSystem("akka-spray")

  /**
   * Ensure that the constructed ActorSystem is shut down when the JVM shuts down
   */
  system.registerOnTermination(system.shutdown())

}
