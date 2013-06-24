package core

import akka.actor.Actor

/**
 * We use the companion object to hold all the messages that the ``RegistrationActor``
 * receives.
 */
object RegistrationActor {

  /**
   * Registers the specified ``user``
   * @param user the user to be registered
   */
  case class Register(user: User)
  case object Registered
  case object NotRegistered

}

/**
 * Registers the users. Replies with
 */
class RegistrationActor extends Actor{
  import RegistrationActor._

  // notice that we don't actually perform any DB operations.
  // that's for another template
  def receive: Receive = {
    case Register(user) if user.email.isEmpty => sender ! Left(NotRegistered)
    case Register(user)                       => sender ! Right(Registered)
  }

}
