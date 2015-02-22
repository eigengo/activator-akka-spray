package api

import akka.actor.ActorRef
import akka.util.Timeout
import core.RegistrationActor.{Register, _}
import core.User
import spray.http._
import spray.routing.Directives

import scala.concurrent.ExecutionContext

class RegistrationService(registration: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats {

  case class ImageUploaded(size: Long)

  import akka.pattern.ask

  import scala.concurrent.duration._
  implicit val timeout = Timeout(2.seconds)

  implicit val userFormat = jsonFormat4(User)
  implicit val registerFormat = jsonFormat1(Register)
  implicit val registeredFormat = jsonObjectFormat[Registered.type]
  implicit val notRegisteredFormat = jsonObjectFormat[NotRegistered.type]
  implicit val imageUploadedFormat = jsonFormat1(ImageUploaded)

  implicit object EitherErrorSelector extends ErrorSelector[NotRegistered.type] {
    def apply(v: NotRegistered.type): StatusCode = StatusCodes.BadRequest
  }

  val route =
    path("register") {
      post {
        handleWith { ru: Register => (registration ? ru).mapTo[Either[NotRegistered.type, Registered.type]] }
      }
    } ~
    path("register" / "image") {
      post {
        handleWith { data: MultipartFormData =>
          data.get("files[]") match {
            case Some(imageEntity) =>
              val size = imageEntity.entity.data.length
              println(s"Uploaded $size")
              ImageUploaded(size)
            case None =>
              println("No files")
              ImageUploaded(0)
          }
        }
      }
    }

}
