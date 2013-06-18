package core

import akka.actor.Actor

object SMSActor {
  case class SendSMS(to: Mobile, message: MessageBody)
}

class SMSActor extends Actor {
  import SMSActor._

  def receive: Receive = {
    case SendSMS((countryCode, number), body) =>
  }
}
