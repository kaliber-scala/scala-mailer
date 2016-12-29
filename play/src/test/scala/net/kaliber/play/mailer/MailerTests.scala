package net.kaliber.play.mailer

import net.kaliber.mailer.{Mailer, MailerSettings}
import org.specs2.mutable.Specification
import play.api.Configuration
import play.api.test.{FakeApplication, WithApplication}
import testUtils.{FullEmail, MailboxUtilities}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Success

object MailerTests extends Specification with TestApplication with FullEmail with MailboxUtilities {

  implicit def ec = play.api.libs.concurrent.Execution.Implicits.defaultContext

  lazy val configuration = Configuration from configurationValues

  "AsyncMailer" should {

    "have a settingFromConfiguration that" >> {
      "correctly converts play Configuration to MailerSettings" in {
        val convertedMailerSettings = net.kaliber.mailer.Session.mailerSettings(configuration.underlying)

        val expectedMailerSettings = MailerSettings(
          Some("smtp"),
          "localhost",
          "10000",
          "toto@localhost",
          Some(true),
          Some("foo"),
          Some("bar")
        )

        expectedMailerSettings === convertedMailerSettings
      }
    }

    "have a fromApplication that" >> {
      "return a Session that can be used with the Mailer" in new TestApp {

        val result = await(new Mailer(Session.fromApplication).sendEmails(simpleEmails))
        result.value === Some(Success(Seq(Success(()), Success(()))))
      }
    }

    "have a fromConfiguration that" >> {
      "return a Session that can be used with the Mailer" in new TestApp {

        val result = await(new Mailer(Session.fromConfiguration(configuration)).sendEmails(simpleEmails))
        result.value === Some(Success(Seq(Success(()), Success(()))))
      }
    }
  }

  def await[T](awaitable: Future[T]) =
    Await.ready(awaitable, Duration.Inf)
}

trait TestApplication extends testUtils.TestApplication {

  lazy val configurationValues = Map(
    "mail.transport.protocol" -> "smtp",
    "mail.host" -> "localhost",
    "mail.port" -> "10000",
    "mail.failTo" -> "toto@localhost",
    "mail.username" -> "foo",
    "mail.password" -> "bar")

  private def f = FakeApplication(additionalConfiguration = configurationValues)

  abstract class TestApp extends WithApplication(f)
}
