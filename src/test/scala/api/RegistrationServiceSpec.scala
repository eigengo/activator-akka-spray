package api

import java.util.UUID

import core.RegistrationActor._
import core.User
import org.specs2.mutable.Specification
import spray.http.{HttpResponse, StatusCodes}
import spray.routing.Directives
import spray.testkit.Specs2RouteTest

class RegistrationServiceSpec extends Specification with Directives with Specs2RouteTest with Core with CoreActors with DefaultJsonFormats {

  private def mkUser(email: String): User = User(UUID.randomUUID(), "A", "B", email)

  implicit val userFormat = jsonFormat4(User)
  implicit val registerFormat = jsonFormat1(Register)

  val badRegistration = Register(mkUser(""))

  val routes = new RegistrationService(registration).route

  "The routing infrastructure should support" >> {
    "the most simple and direct route" in {
      Get() ~> complete(HttpResponse()) ~> (_.response) === HttpResponse()
    }
  }

  "proper result status setting" in {
    Post("/register", badRegistration) ~> routes ~> check {
      handled.aka("register_request_is_handled") must beTrue
      status must_== StatusCodes.BadRequest
    }
  }
}
