package core

import akka.actor.Actor
import core.EmailActor.SendEmail

object EmailActor {
  case class SendEmail(to: String, message: String)
}

class EmailActor extends Actor {

  def receive: Receive = {
    case SendEmail(to, body) =>
  }
}
