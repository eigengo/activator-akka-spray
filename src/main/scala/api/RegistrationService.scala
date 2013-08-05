package api

import spray.routing.Directives
import scala.concurrent.ExecutionContext
import akka.actor.ActorRef
import core.{User, RegistrationActor}
import akka.util.Timeout
import RegistrationActor._
import spray.http._
import core.User
import core.RegistrationActor.Register
import scala.Some

class RegistrationService(registration: ActorRef)(implicit executionContext: ExecutionContext)
  extends Directives with DefaultJsonFormats {

  case class ImageUploaded(size: Int)

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
          data.fields.get("files[]") match {
            case Some(imageEntity) =>
              val size = imageEntity.entity.buffer.length
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
