package api

import spray.routing.Directives
import scala.concurrent.ExecutionContext
import akka.actor.ActorRef

class RegistrationService(registration: ActorRef)(implicit executionContext: ExecutionContext) extends Directives {

  val route =
    path("registration") {
      post {
        complete {
          "Yay!"
        }
      }
    }

}
