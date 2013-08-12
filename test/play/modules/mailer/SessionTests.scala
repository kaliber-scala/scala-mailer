package play.modules.mailer

import org.specs2.mutable.Specification

import play.api.test.FakeApplication

object SessionTests extends Specification {

  lazy val configuration = Map(
    "mail.transport.protocol" -> "protocol",
    "mail.smtp.host" -> "localhost",
    "mail.smtp.port" -> "10000",
    "mail.smtp.failTo" -> "toto@localhost",
    "mail.smtp.ssl.enable" -> "false",
    "mail.smtp.username" -> "foo",
    "mail.smtp.password" -> "bar")

  def configuredApplication = FakeApplication(
    path = new java.io.File("./test/"),
    additionalConfiguration = configuration)

  "Session" should {

    "have have the correct default protocol" in {

       new Session.Keys()(FakeApplication()).protocol === "smtps"
    }

    "have ssl enabled by default" in {
      
      new Session.Keys()(FakeApplication()).sslEnable === "true"
    }

    "extract the correct information from the configuration" in {
      
      val session = Session.fromConfiguration(configuredApplication)

      val properties = session.getProperties
      
      def p = properties.getProperty(_:String)
      
      p("mail.transport.protocol") === "protocol"
      p("mail.smtps.quitwait") === "false"
      p("mail.smtps.host") === "localhost"
      p("mail.smtps.port") === "10000"
      p("mail.smtp.ssl.enable") === "false"
      p("mail.smtp.from") === "toto@localhost" 
      p("mail.smtps.username") === "foo"
      p("mail.smtps.auth") === "true"
      
      val authentication = session.requestPasswordAuthentication(null, 0, null, null, null)
      authentication.getUserName === "foo"
      authentication.getPassword === "bar"
    }

  }

}