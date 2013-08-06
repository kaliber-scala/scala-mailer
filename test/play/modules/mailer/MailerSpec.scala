package play.modules.mailer

import org.jvnet.mock_javamail.Mailbox
import org.specs2.matcher.MatchResult
import org.specs2.mutable.{ BeforeAfter, Specification }
import javax.mail.internet.InternetAddress
import play.api.test.FakeApplication
import javax.mail.Message
import scala.util.{ Failure, Try, Success }
import scala.collection.JavaConverters._

class MailerSpec extends Specification with BeforeAfter with TestConfiguration {

  def sendDefaultMail() =
    Mailer sendEmail testEmail()

  def withMessage(matchResult: Message => MatchResult[_]): MatchResult[Any] =
    (defaultInbox.size === 1).setMessage("No message available to test") and
      matchResult(defaultInbox get 0)

  "Mailer" should {

    sendDefaultMail()

    "send an email" in {
      defaultInbox.size === 1
    }

    "with subject Test mail" in {
      withMessage(_.getSubject === "Test mail")
    }

    "with from name Erik Westra sender" in {
      withMessage { message =>
        val from = message.getFrom

        from.size === 1 and {
          val fromAddress = from(0)

          fromAddress must beAnInstanceOf[InternetAddress] and {
            val typedFromAddress = fromAddress.asInstanceOf[InternetAddress]

            typedFromAddress.getAddress === "ewestra@rhinofly.nl" and
              typedFromAddress.getPersonal === "Erik Westra sender"
          }
        }
      }
    }

    "send multiple emails" in {

      val inbox = Mailbox.get("jean.helou@gmail.com")

      val email =
        testEmail(
          subject = "Test mail for batch",
          from = EmailAddress("Jean Helou sender", "jean.helou@gmail.com"),
          recipient = EmailAddress("Jean Helou receiver", "jean.helou@gmail.com"))

      val emails = Mailer.sendEmails(email :: email :: Nil)

      val subjects = "Test mail for batch" :: "Test mail for batch" :: Nil

      emails must beLike {
        case Success(Success(_) :: Success(_) :: Nil) => ok 
      }

      inbox.size() === 2
      inbox.asScala.map(_.getSubject) === subjects
    }

    "send multiple emails handling errors" in {

      val inbox = Mailbox.get("jean.helou+success@gmail.com")
      val errorInbox = Mailbox.get("jean.helou+error@gmail.com")

      errorInbox.setError(true)

      val email = testEmail(
        subject = "Test mail for batch error",
        from = EmailAddress("Jean Helou sender", "jean.helou@gmail.com"),
        recipient = EmailAddress("Jean Helou receiver", "jean.helou+success@gmail.com"))

      val errorEmail = email.copy(recipients = List(Recipient(RecipientType.TO,
        EmailAddress("Jean Helou error receiver", "jean.helou+error@gmail.com"))))

      val emails = Mailer.sendEmails(email :: errorEmail :: Nil)

      emails must beLike {
        case Success(Success(_) :: Failure(_) :: Nil) => ok 
      }

      inbox.size() === 1
      errorInbox.size() === 0
    }

  }
}