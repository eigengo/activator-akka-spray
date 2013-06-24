package api

import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import spray.routing.Directives
import core.MessengerActor

class MessengerService(messenger: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats {

  import MessengerActor._

  implicit val sendMessageFormat = jsonFormat2(SendMessage)

   val route =
     path("message") {
       post {
         handleWith { sm: SendMessage => messenger ! sm; "{}" }
       }
     }

}