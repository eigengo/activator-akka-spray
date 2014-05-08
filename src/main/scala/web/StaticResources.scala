package web

import spray.routing.HttpService
import spray.http.StatusCodes

/**
 * Created by antoine on 2/25/14.
 */
trait StaticResources extends HttpService {
  val staticResources = get {
    path("") {
      redirect("/index.html", StatusCodes.MovedPermanently)
    } ~
      path("favicon.ico") {
        complete(StatusCodes.NotFound)
      } ~ path(Rest) {
      path => getFromResource("root/%s" format path)
    }

  }

}
