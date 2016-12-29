package net.kaliber.mailer

import java.util.Properties
import javax.mail.NoSuchProviderException

import org.specs2.mutable.Specification
import testUtils.{FullEmail, MailboxUtilities, TestApplication, TestSettings}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

object AsyncMailerTests extends Specification with TestApplication
  with FullEmail with MailboxUtilities {

  "AsyncMailer" should {
    "have a method sendMail that" >> {

      "correctly converts a failure to a failed future" in {

        val session = javax.mail.Session.getInstance(new Properties())
        val mailer = new Mailer(session)

        val result = await(mailer.sendEmail(simpleEmail))
        result.value must beLike {
          case Some(Failure(SendEmailException(email, t))) =>
            email === simpleEmail
            t must beAnInstanceOf[NoSuchProviderException]
        }
      }

      "correctly converts a success to a succeeded future" in {

        val result = await(new Mailer(Session.fromSetting(TestSettings.mailerSettings)).sendEmail(simpleEmail))
        result.value === Some(Success(()))
      }
    }

    "have a method sendMails that" >> {
      "correctly converts a failure to a failed future" in {

        val session = javax.mail.Session.getInstance(new Properties())
        val mailer = new Mailer(session)

        val result = await(mailer.sendEmails(simpleEmails))
        result.value must beLike {
          case Some(Failure(SendEmailsException(emails, t))) =>
            emails === simpleEmails
            t must beAnInstanceOf[NoSuchProviderException]
        }
      }

      "correctly converts a success to a succeeded future" in {

        val result = await(new Mailer(Session.fromSetting(TestSettings.mailerSettings)).sendEmails(simpleEmails))
        result.value === Some(Success(Seq(Success(()), Success(()))))
      }
    }
  }

  def await[T](awaitable: Future[T]) =
    Await.ready(awaitable, Duration.Inf)

}