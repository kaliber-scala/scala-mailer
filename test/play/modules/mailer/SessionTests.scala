package play.modules.mailer

import org.specs2.mutable.Specification

import play.api.test.FakeApplication

object SessionTests extends Specification {

  lazy val configuration = Map(
    "mail.transport.protocol" -> "protocol",
    "mail.host" -> "localhost",
    "mail.port" -> "10000",
    "mail.failTo" -> "toto@localhost",
    "mail.username" -> "foo",
    "mail.password" -> "bar")

  def configuredApplication = FakeApplication(
    path = new java.io.File("./test/"),
    additionalConfiguration = configuration)

  "Session" should {

    "have have the correct default protocol" in {

       new Session.Keys()(FakeApplication()).protocol === "smtps"
    }

    "extract the correct information from the configuration" in {
      
      val session = Session.fromConfiguration(configuredApplication)

      val properties = session.getProperties
      
      def p = properties.getProperty(_:String)
      
      p("mail.transport.protocol") === "protocol"
      p("mail.protocol.quitwait") === "false"
      p("mail.protocol.host") === "localhost"
      p("mail.protocol.port") === "10000"
      p("mail.protocol.from") === "toto@localhost" 
      p("mail.protocol.auth") === "true"
      
      val authentication = session.requestPasswordAuthentication(null, 0, null, null, null)
      authentication.getUserName === "foo"
      authentication.getPassword === "bar"
    }
    
  }

}