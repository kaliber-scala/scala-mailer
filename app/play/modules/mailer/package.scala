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

  type Session = javax.mail.Session

  object Session {

    class Keys(implicit app: Application) {
      
      lazy val protocol = PlayConfiguration("mail.transport.protocol", default = "smtps")
      lazy val host = PlayConfiguration("mail.host")
      lazy val port = PlayConfiguration("mail.port")
      lazy val username = PlayConfiguration("mail.username")
      lazy val password = PlayConfiguration("mail.password")
      lazy val failTo = PlayConfiguration("mail.failTo")
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
      properties.put(s"mail.$protocol.auth", "true")

      val username = keys.username
      val password = keys.password

      javax.mail.Session.getInstance(properties, new Authenticator {

        override def getPasswordAuthentication = new PasswordAuthentication(username, password)
      })
    }
  }
}

