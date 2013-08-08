package play.modules.mailer

import org.specs2.mutable.Specification
import java.util.Properties
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.util.Failure
import javax.mail.NoSuchProviderException
import scala.util.Success

object AsyncMailerTests extends Specification with TestApplication
  with FullEmail with MailboxUtilities {

  implicit val ec = play.api.libs.concurrent.Execution.Implicits.defaultContext

  "AsyncMailer" should {

    "have the correct default instance" in {
      AsyncMailer must beAnInstanceOf[AsyncMailer]
      AsyncMailer.mailer === Mailer
    }

    "have a method sendMail that" >> {

      "correctly converts a failure to a failed future" in {

        val session = javax.mail.Session.getInstance(new Properties())
        val mailer = new AsyncMailer(new Mailer(session))

        val result = await(mailer.sendEmail(simpleEmail))
        result.value must beLike {
          case Some(Failure(SendEmailException(email, t))) =>
            email === simpleEmail
            t must beAnInstanceOf[NoSuchProviderException]
        }
      }

      "correctly converts a success to a succeeded future" in {

        val result = await(AsyncMailer.sendEmail(simpleEmail))
        result.value === Some(Success())
      }
    }

    "have a method sendMails that" >> {

      "correctly converts a failure to a failed future" in {

        val session = javax.mail.Session.getInstance(new Properties())
        val mailer = new AsyncMailer(new Mailer(session))

        val result = await(mailer.sendEmails(simpleEmails))
        result.value must beLike {
          case Some(Failure(SendEmailsException(emails, t))) =>
            emails === simpleEmails
            t must beAnInstanceOf[NoSuchProviderException]
        }
      }

      "correctly converts a success to a succeeded future" in {

        val result = await(AsyncMailer.sendEmails(simpleEmails))
        result.value === Some(Success(Seq(Success(), Success())))
      }
    }
  }

  def await[T](awaitable: Future[T]) =
    Await.ready(awaitable, Duration.Inf)

}