package net.kaliber.mailer

import java.util.Properties

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Failure
import scala.util.Success

import org.specs2.mutable.Specification

import javax.mail.NoSuchProviderException

import testUtils.TestApplication
import testUtils.FullEmail
import testUtils.MailboxUtilities

object AsyncMailerTests extends Specification with TestApplication
  with FullEmail with MailboxUtilities {
  sequential

  implicit def ec = play.api.libs.concurrent.Execution.Implicits.defaultContext

  "AsyncMailer" should {

    "have a method sendMail that" >> {

      "correctly converts a failure to a failed future" in new TestApp {

        val session = javax.mail.Session.getInstance(new Properties())
        val mailer = new Mailer(session)

        val result = await(mailer.sendEmail(simpleEmail))
        result.value must beLike {
          case Some(Failure(SendEmailException(email, t))) =>
            email === simpleEmail
            t must beAnInstanceOf[NoSuchProviderException]
        }
      }

      "correctly converts a success to a succeeded future" in new TestApp {

        val result = await(new Mailer(Session.fromApplication).sendEmail(simpleEmail))
        result.value === Some(Success(()))
      }
    }

    "have a method sendMails that" >> {

      "correctly converts a failure to a failed future" in new TestApp {

        val session = javax.mail.Session.getInstance(new Properties())
        val mailer = new Mailer(session)

        val result = await(mailer.sendEmails(simpleEmails))
        result.value must beLike {
          case Some(Failure(SendEmailsException(emails, t))) =>
            emails === simpleEmails
            t must beAnInstanceOf[NoSuchProviderException]
        }
      }

      "correctly converts a success to a succeeded future" in new TestApp {

        val result = await(new Mailer(Session.fromApplication).sendEmails(simpleEmails))
        result.value === Some(Success(Seq(Success(()), Success(()))))
      }
    }
  }

  def await[T](awaitable: Future[T]) =
    Await.ready(awaitable, Duration.Inf)

}
