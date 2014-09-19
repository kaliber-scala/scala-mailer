package play.modules

import java.util.Properties

import javax.mail.Authenticator
import javax.mail.PasswordAuthentication
import play.api.Application

package object mailer {

  type DataSource = javax.activation.DataSource

  type RecipientType = javax.mail.Message.RecipientType
  object RecipientType {
    val TO: RecipientType = javax.mail.Message.RecipientType.TO
    val BCC: RecipientType = javax.mail.Message.RecipientType.BCC
    val CC: RecipientType = javax.mail.Message.RecipientType.CC
  }

  lazy val Mailer = new Mailer(Session.fromConfiguration(play.api.Play.current))
  
  type Session = javax.mail.Session

  object Session {

    class Keys(implicit app: Application) {

      lazy val protocol = PlayConfiguration("mail.transport.protocol", default = "smtps")
      lazy val host = PlayConfiguration("mail.host")
      lazy val port = PlayConfiguration("mail.port")
      lazy val username = PlayConfiguration("mail.username")
      lazy val password = PlayConfiguration("mail.password")
      lazy val failTo = PlayConfiguration("mail.failTo")
      lazy val auth = PlayConfiguration("mail.auth", default = "true")
    }

    def fromConfiguration(implicit app: Application): Session = {

      val keys = new Keys
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

