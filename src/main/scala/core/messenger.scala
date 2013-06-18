package core

import akka.actor.{Props, Actor}
import java.util.UUID

object MessengerActor {

  case class SendMessage(to: UUID, message: String)

}

class MessengerActor extends Actor {
  import MessengerActor._
  import EmailActor._
  import SMSActor._

  val email = context.actorOf(Props[EmailActor])
  val sms   = context.actorOf(Props[SMSActor])

  def receive: Receive = {
    case SendMessage(to, message) if to.getLeastSignificantBits % 2 == 0 =>
      email ! SendEmail("foo@bar.com", message)
    case SendMessage(to, message) if to.getLeastSignificantBits % 2 != 0 =>
      sms ! SendSMS("+447771234567", message)
  }
}
