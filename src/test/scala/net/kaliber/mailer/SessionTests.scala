package net.kaliber.mailer

import org.specs2.mutable.Specification
import play.api.test.FakeApplication
import play.api.Configuration

object SessionTests extends Specification {

  lazy val configurationValues = Map(
    "mail.transport.protocol" -> "protocol",
    "mail.host" -> "localhost",
    "mail.port" -> "10000",
    "mail.failTo" -> "toto@localhost",
    "mail.username" -> "foo",
    "mail.password" -> "bar"
  )

  lazy val configuration = Configuration from configurationValues

  "Session" should {

    "have have the correct default protocol" in {

      new Session.Keys(Configuration.empty).protocol === "smtps"
    }

    "have have the correct default value for auth" in {

      new Session.Keys(Configuration.empty).auth === "true"
    }

    "extract the correct information from the configuration" in {

      val session = Session.fromConfiguration(configuration)

      val properties = session.getProperties

      def p = properties.getProperty(_: String)

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

    "allow for a configuration that disables authentication" in {

      lazy val configurationWithoutAuth =
        configurationValues - ("mail.username") - ("mail.password") + ("mail.auth" -> "false")

      val session = Session.fromConfiguration(Configuration from configurationWithoutAuth)

      val properties = session.getProperties

      def p = properties.getProperty(_: String)

      p("mail.protocol.auth") === "false"

      val authentication = session.requestPasswordAuthentication(null, 0, null, null, null)
      authentication === null
    }

  }

}
