package core

import spray.json.{JsonFormat, JsValue, JsString, DefaultJsonProtocol, deserializationError}
import java.util.UUID

/**
 * Contains useful JSON formats: ``j.u.Date``, ``j.u.UUID`` and others
 */
trait DefaultJsonFormats extends DefaultJsonProtocol {

  implicit object UuidJsonFormat extends JsonFormat[UUID] {
    def write(x: UUID) = JsString(x.toString)
    def read(value: JsValue) = value match {
      case JsString(x) => UUID.fromString(x)
      case x           => deserializationError("Expected UUID as JsString, but got " + x)
    }
  }

}
