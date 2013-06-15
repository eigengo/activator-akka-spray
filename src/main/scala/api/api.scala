package api

import core.{CoreActors, Core}

/**
 * The REST API layer. It exposes the REST services, but does not provide any
 * web server interface.
 * Notice that it requires to be mixed in with ``core.Core``.
 */
trait Api {
  this: CoreActors =>

}
