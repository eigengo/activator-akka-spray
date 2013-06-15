import akka.actor.ActorSystem

/**
 * Contains useful type aliases that are to be members of the entire ``core`` package.
 */
package object core {

  /**
   * Email is a plain old String.
   */
  type Email = String

  /**
   * Mobile number is a tuple: country code and local number
   */
  type Mobile = (String, String)

  /**
   * MessageBody is also just plain old String.
   */
  type MessageBody = String

  /**
   * Core is a structural type containing the ``system: ActorSystem`` member.
   */
  type Core = { def system: ActorSystem }

}
