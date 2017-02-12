package testUtils

import net.kaliber.mailer.{MailerSettings, Session}
import org.jvnet.mock_javamail.Mailbox
import org.specs2.mutable.After

object TestSettings {

  lazy val mailerSettings = MailerSettings(
    Some("smtp"),
    "localhost",
    "10000",
    "toto@localhost",
    Some(false),
    None,
    None
  )

  lazy val defaultSession = Session.fromSetting(TestSettings.mailerSettings)

  lazy val mailerSettingsWithAuth = MailerSettings(
    Some("smtp"),
    "localhost",
    "10000",
    "toto@localhost",
    Some(true),
    Some("foo"),
    Some("bar")
  )
}

trait TestApplication extends After {
  def after = {
    Mailbox.clearAll()
  }
}
