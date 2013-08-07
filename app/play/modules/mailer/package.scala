package play.modules

import java.util.Properties
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication
import play.modules.mailer.PlayConfiguration
import play.api.Play.current

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

    object keys {
      lazy val protocol = PlayConfiguration("mail.transport.protocol", default = "smtps")
      lazy val sslEnable = PlayConfiguration("mail.smtp.ssl.enable", default = "true")
      lazy val host = PlayConfiguration("mail.smtp.host")
      lazy val port = PlayConfiguration("mail.smtp.port")
      lazy val username = PlayConfiguration("mail.smtp.username")
      lazy val password = PlayConfiguration("mail.smtp.password")
      lazy val failTo = PlayConfiguration("mail.smtp.failTo")
    }

    lazy val fromConfiguration: Session = {

      val properties = new Properties()
      properties.put("mail.transport.protocol", keys.protocol)
      properties.put("mail.smtps.quitwait", "false")
      properties.put("mail.smtps.host", keys.host)
      properties.put("mail.smtps.port", keys.port)
      properties.put("mail.smtp.ssl.enable", keys.sslEnable)
      properties.put("mail.smtp.from", keys.failTo)

      val username = keys.username
      val password = keys.password

      properties.put("mail.smtps.username", username)
      properties.put("mail.smtps.auth", "true")

      javax.mail.Session.getInstance(properties, new Authenticator {

        override def getPasswordAuthentication = new PasswordAuthentication(username, password)

      })
    }
  }
}

