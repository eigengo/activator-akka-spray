package core

import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.ActorSystem
import org.specs2.mutable.SpecificationLike
import java.util.UUID

class RegistrationActorSpec extends TestKit(ActorSystem()) with SpecificationLike with CoreActors with Core with ImplicitSender {
  import RegistrationActor._

  private def mkUser(email: String): User = User(UUID.randomUUID(), "A", "B", email)

  sequential

  "Registration should" >> {

    "reject invalid email" in {
      registration ! Register(mkUser(""))
      expectMsg(Left(NotRegistered))
      success
    }

    "accept valid user to be registered" in {
      registration ! Register(mkUser("jan@eigengo.com"))
      expectMsg(Right(Registered))
      success
    }
  }

}
