package api

import akka.actor.ActorRef
import scala.concurrent.ExecutionContext
import spray.routing.Directives

class EmailService(messenger: ActorRef)(implicit executionContext: ExecutionContext) extends Directives {

   val route =
     path("email") {
       post {
         complete {
           "e-mail"
         }
       }
     }

 }
