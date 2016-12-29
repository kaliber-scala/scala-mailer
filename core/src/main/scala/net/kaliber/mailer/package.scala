package net.kaliber

import java.util.Properties
import javax.mail.{Authenticator, PasswordAuthentication}

import com.typesafe.config.Config

package object mailer {

  type DataSource = javax.activation.DataSource

  type RecipientType = javax.mail.Message.RecipientType
  object RecipientType {
    val TO: RecipientType = javax.mail.Message.RecipientType.TO
    val BCC: RecipientType = javax.mail.Message.RecipientType.BCC
    val CC: RecipientType = javax.mail.Message.RecipientType.CC
  }

  type Session = javax.mail.Session

  case class MailerSettings(
     protocol: Option[String],
     host: String,
     port: String,
     failTo: String,
     auth: Option[Boolean],
     username: Option[String],
     password: Option[String]
  )

  object Session {

    def mailerSettings(config: Config): MailerSettings = {
      def error(key: String) = throw new RuntimeException(s"Could not find $key in settings")

      def getStringOption(key: String): Option[String] =
        if(config.hasPath(key)) Some(config getString key) else None

      def getString(key: String, default: Option[String] = None): String =
        getStringOption(key) orElse default getOrElse error(key)

      def getStringWithFallback(key: String, fallback: String): String = getString(key, Some(fallback))

      MailerSettings(
        Some(getStringWithFallback("mail.transport.protocol", fallback = "smtps")),
        getString("mail.host"),
        getString("mail.port"),
        getString("mail.failTo"),
        Some(getStringWithFallback("mail.auth", fallback = "true").toBoolean),
        getStringOption("mail.username"),
        getStringOption("mail.password")
      )
    }

    def fromConfig(config: Config): Session =
      fromSetting(mailerSettings(config))

    def fromSetting(setting: MailerSettings): Session = {
      val protocol = setting.protocol.getOrElse("smtps")
      val auth = setting.auth.getOrElse(true)

      val properties = new Properties()
      properties.put(s"mail.transport.protocol", protocol)
      properties.put(s"mail.$protocol.quitwait", "false")
      properties.put(s"mail.$protocol.host", setting.host)
      properties.put(s"mail.$protocol.port", setting.port)
      properties.put(s"mail.$protocol.from", setting.failTo)
      properties.put(s"mail.$protocol.auth", auth.toString)

      val authenticator =
        if (auth) {

          val username = setting.username.getOrElse(throw new RuntimeException("username is expected in te MailerSettings"))
          val password = setting.password.getOrElse(throw new RuntimeException("password is expected in te MailerSettings"))

          new Authenticator {
            override def getPasswordAuthentication = new PasswordAuthentication(username, password)
          }
        } else null

      javax.mail.Session.getInstance(properties, authenticator)
    }
  }
}

