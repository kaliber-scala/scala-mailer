package net.kaliber.mailer

import org.specs2.mutable.Specification
import testUtils.TestSettings.{mailerSettings, _}

object SessionTests extends Specification {

  "Session" should {

    "have have the correct default protocol" in {
      val settings = MailerSettings(None, "", "", "", Some(false), None, None)

      val session = Session.fromSetting(settings)

      val properties = session.getProperties

      def p = properties.getProperty(_: String)

      p("mail.transport.protocol") === "smtps"
    }

    "have have the correct default value for auth" in {
      val settings = MailerSettings(Some("protocol"), "", "", "", None, Some("for"), Some("bar"))

      val session = Session.fromSetting(settings)

      val properties = session.getProperties

      def p = properties.getProperty(_: String)

      p("mail.protocol.auth") === "true"
    }

    "extract the correct information from the mailerSettings" in {
      val expectedMailerSettings = mailerSettingsWithAuth.copy(protocol = Some("protocol"))

      val session = Session.fromSetting(expectedMailerSettings)

      val properties = session.getProperties

      def p = properties.getProperty(_: String)

      p("mail.transport.protocol") === expectedMailerSettings.protocol.get
      p("mail.protocol.quitwait") === "false"
      p("mail.protocol.host") === expectedMailerSettings.host
      p("mail.protocol.port") === expectedMailerSettings.port
      p("mail.protocol.from") === expectedMailerSettings.failTo
      p("mail.protocol.auth") === expectedMailerSettings.auth.get.toString

      val authentication = session.requestPasswordAuthentication(null, 0, null, null, null)
      authentication.getUserName === expectedMailerSettings.username.get
      authentication.getPassword === expectedMailerSettings.password.get
    }

    "allow for a configuration that disables authentication" in {
      val session = Session.fromSetting(mailerSettings.copy(protocol = Some("protocol")))

      val properties = session.getProperties

      def p = properties.getProperty(_: String)

      p("mail.protocol.auth") === "false"

      val authentication = session.requestPasswordAuthentication(null, 0, null, null, null)
      authentication === null
    }

  }
}

