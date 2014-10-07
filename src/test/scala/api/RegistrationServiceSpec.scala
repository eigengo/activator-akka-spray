package api

import spray.testkit.Specs2RouteTest
import spray.routing.Directives
import org.specs2.mutable.Specification
import spray.http.{HttpResponse, StatusCodes}
import core.{Core, CoreActors, User}
import core.RegistrationActor._
import java.util.UUID
import spray.httpx.SprayJsonSupport

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

    "proper result status setting" in {
      Post("/register", badRegistration) ~> routes ~> check {
        handled.aka("register_request_is_handled") must beTrue
        status must_== StatusCodes.BadRequest
      }
    }
  }
}
