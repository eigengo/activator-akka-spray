package core

import java.util.UUID

/**
 * A "typical" user value containing its identity, name and email. Notice
 * that we use the ``EmailAddress`` type alias from the ``core`` package object.
 *
 * @param id the identity
 * @param firstName the first name
 * @param lastName the last name
 * @param email the email address
 */
case class User(id: UUID, firstName: String, lastName: String, email: Email)

/**
 * The JSON formats for the users. Follow the ``json-pickler`` template to avoid having
 * to do as much typing.
 */
trait UserFormats extends DefaultJsonFormats {
  implicit val userFormat = jsonFormat4(User)
}