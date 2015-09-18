package net.kaliber

import java.util.Properties
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication
import play.api.Application
import play.api.Configuration
import play.api.PlayException

package object mailer {

  type DataSource = javax.activation.DataSource

  type RecipientType = javax.mail.Message.RecipientType
  object RecipientType {
    val TO: RecipientType = javax.mail.Message.RecipientType.TO
    val BCC: RecipientType = javax.mail.Message.RecipientType.BCC
    val CC: RecipientType = javax.mail.Message.RecipientType.CC
  }

  type Session = javax.mail.Session

  object Session {

    class Keys(configuration: Configuration) {

      def error(key: String) = throw new PlayException("Configuration error", s"Could not find $key in settings")

      def getString(key: String, default: Option[String] = None): String =
        configuration getString key orElse default getOrElse error(key)

      def getString(key: String, default: String): String = getString(key, Some(default))

      lazy val protocol = getString("mail.transport.protocol", default = "smtps")
      lazy val host = getString("mail.host")
      lazy val port = getString("mail.port")
      lazy val username = getString("mail.username")
      lazy val password = getString("mail.password")
      lazy val failTo = getString("mail.failTo")
      lazy val auth = getString("mail.auth", default = "true")
    }

    def fromApplication(implicit app: Application): Session = fromConfiguration(app.configuration)

    def fromConfiguration(configuration: Configuration): Session = {

      val keys = new Keys(configuration)
      val protocol = keys.protocol

      val properties = new Properties()
      properties.put(s"mail.transport.protocol", protocol)
      properties.put(s"mail.$protocol.quitwait", "false")
      properties.put(s"mail.$protocol.host", keys.host)
      properties.put(s"mail.$protocol.port", keys.port)
      properties.put(s"mail.$protocol.from", keys.failTo)
      properties.put(s"mail.$protocol.auth", keys.auth)

      val authenticator =
        if (keys.auth.toBoolean) {

          val username = keys.username
          val password = keys.password

          new Authenticator {
            override def getPasswordAuthentication = new PasswordAuthentication(username, password)
          }
        } else null

      javax.mail.Session.getInstance(properties, authenticator)
    }
  }
}

