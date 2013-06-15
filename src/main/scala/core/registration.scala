package core

import akka.actor.Actor

object RegistrationActor {

  case class Register(user: User)

}

/**
 * Registers the users
 */
class RegistrationActor extends Actor{
  import RegistrationActor._

  def receive = {
    case Register(user) =>

  }

}
