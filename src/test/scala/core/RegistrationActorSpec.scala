package core

import akka.testkit.{ImplicitSender, TestKit}
import akka.actor.ActorSystem
import org.specs2.mutable.SpecificationLike
import java.util.UUID

class RegistrationActorSpec extends TestKit(ActorSystem()) with ImplicitSender with SpecificationLike with CoreActors with Core {
  import RegistrationActor._

  private def mkUser(email: String): User = User(UUID.randomUUID(), "A", "B", email)

  sequential

  "Registration should" >> {

    "reject invalid email" in {
      registration ! Register(mkUser(""))
      expectMsgType[NotRegistered.type]
      success
    }

    "accept valid user to be registered" in {
      registration ! Register(mkUser("jan@eigengo.com"))
      expectMsgType[Registered.type]
      success
    }
  }

}
