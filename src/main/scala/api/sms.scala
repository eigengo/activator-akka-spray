package api

import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import spray.routing.Directives

class SMSService(messenger: ActorRef)(implicit executionContext: ExecutionContext) extends Directives {

  val route =
    path("sms") {
      post {
        complete {
          "SMS"
        }
      }
    }

}
