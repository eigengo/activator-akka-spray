package api

import core.{CoreActors, Core}

/**!
 * The REST API layer. It exposes the REST services, but does not provide any
 * web server interface.<br/>
 * Notice that it requires to be mixed in with ``core.Core``, which provides access
 * to the top-level actors that make up the system.
 */
trait Api {
  this: CoreActors =>

}
