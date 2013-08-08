package play.modules.mailer

import org.specs2.mutable.Specification

import play.api.test.FakeApplication

object SessionTests extends Specification {

  lazy val configuration = Map(
    "mail.transport.protocol" -> "smtpa",
    "mail.smtp.host" -> "localhost",
    "mail.smtp.port" -> "10000",
    "mail.smtp.failTo" -> "toto@localhost",
    "mail.smtp.username" -> "foo",
    "mail.smtp.password" -> "bar")

  def configuredApplication = FakeApplication(
    path = new java.io.File("./test/"),
    additionalConfiguration = configuration)

  /*
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
  */

  "Session" should {

    "extract the correct information from the configuration" in {
    	todo
    }

  }

}