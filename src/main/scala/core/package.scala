import akka.actor.ActorSystem

/**
 * Contains useful type aliases that are to be members of the entire ``core`` package.
 */
package object core {

  /**
   * EmailAddress is a plain old String.
   */
  type EmailAddress = String

  type Core = { def system: ActorSystem }

}
