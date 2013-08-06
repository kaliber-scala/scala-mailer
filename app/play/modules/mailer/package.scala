package play.modules

import javax.mail.Authenticator
import java.util.Properties
import javax.mail.PasswordAuthentication

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
    
    implicit def fromConfiguration:Session = {
    
      import Mailer.keys
      
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

