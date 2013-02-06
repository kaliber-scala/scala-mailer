package play.modules.mailer

import org.jvnet.mock_javamail.Mailbox
import org.specs2.matcher.MatchResult
import org.specs2.mutable.Before
import org.specs2.mutable.Specification

import javax.mail.Message
import javax.mail.internet.InternetAddress
import play.api.test.FakeApplication

class MailerSpec extends Specification with Before {
  lazy val configuration = Map(
    "mail.transport.protocol" -> "smtp",
    "mail.smtp.host" -> "localhost",
    "mail.smtp.port" -> "10000",
    "mail.smtp.failTo" -> "toto@localhost",
    "mail.smtp.username" -> "foo",
    "mail.smtp.password" -> "bar")

  def f = FakeApplication(path = new java.io.File("./test/"), additionalConfiguration = configuration)

  def before = play.api.Play.start(f)

  val inbox = {
    val inbox = Mailbox.get("ewestra@rhinofly.nl");
    Mailer.sendEmail(Email(
      subject = "Test mail",
      from = EmailAddress("Erik Westra sender", "ewestra@rhinofly.nl"),
      replyTo = None,
      recipients = List(Recipient(Message.RecipientType.TO, EmailAddress("Erik Westra recipient", "ewestra@rhinofly.nl"))),
      text = "text",
      htmlText = "htmlText",
      attachments = Seq.empty))

    inbox
  }

  def withMessage[T](matchResult: Message => MatchResult[T]) =
    inbox.size must be_==(1).setMessage("No message available to test") and matchResult(inbox.get(0))

  "Ses" should {

    "send an email" in {
      inbox.size() === 1
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
  }
}