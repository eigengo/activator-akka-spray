package core

import akka.actor.Actor
import core.EmailActor.SendEmail

object EmailActor {
  case class SendEmail(to: Email, message: MessageBody)
}

class EmailActor extends Actor {

  def receive: Receive = {
    case SendEmail(to, body) =>
  }
}
