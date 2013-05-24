package play.modules.mailer

import org.jvnet.mock_javamail.Mailbox
import org.specs2.matcher.MatchResult
import org.specs2.mutable.{BeforeAfter, Specification}

import javax.mail.internet.InternetAddress
import play.api.test.FakeApplication
import javax.mail.Message
import scala.util.{Failure, Try, Success}

class MailerSpec extends Specification with BeforeAfter {
  lazy val configuration = Map(
    "mail.transport.protocol" -> "smtp",
    "mail.smtp.host" -> "localhost",
    "mail.smtp.port" -> "10000",
    "mail.smtp.failTo" -> "toto@localhost",
    "mail.smtp.username" -> "foo",
    "mail.smtp.password" -> "bar")

  def f = FakeApplication(path = new java.io.File("./test/"), additionalConfiguration = configuration)

  def before { play.api.Play.start(f) }
  def after { Mailbox.clearAll() }

  val inbox = {
    val inbox = Mailbox.get("ewestra@rhinofly.nl")
    Mailer.sendEmail(Email(
      subject = "Test mail",
      from = EmailAddress("Erik Westra sender", "ewestra@rhinofly.nl"),
      replyTo = None,
      recipients = List(Recipient(RecipientType.TO, EmailAddress("Erik Westra recipient", "ewestra@rhinofly.nl"))),
      text = "text",
      htmlText = "htmlText",
      attachments = Seq.empty))

    inbox
  }

  case class SentBatch(emails:Seq[Try[Email]],inbox:Mailbox, errors:Mailbox = Mailbox.get("error@localhost"))
  val batch={
    val inbox = Mailbox.get("jean.helou@gmail.com")

    val email = Email(
      subject = "Test mail for batch",
      from = EmailAddress("Jean Helou sender", "jean.helou@gmail.com"),
      replyTo = None,
      recipients = List(Recipient(RecipientType.TO, EmailAddress("Jean Helou receiver", "jean.helou@gmail.com"))),
      text = "text",
      htmlText = "htmlText",
      attachments = Seq.empty)
    val emails = Mailer.sendEmails(email :: email :: Nil)
    SentBatch( emails,inbox )
  }
  val batchWithError={
    val inbox = Mailbox.get("jean.helou+success@gmail.com")
    val errorInbox = Mailbox.get("jean.helou+error@gmail.com")

    errorInbox.setError(true)
    val email = Email(
      subject = "Test mail for batch error",
      from = EmailAddress("Jean Helou sender", "jean.helou@gmail.com"),
      replyTo = None,
      recipients = List(Recipient(RecipientType.TO, EmailAddress("Jean Helou receiver", "jean.helou+success@gmail.com"))),
      text = "text",
      htmlText = "htmlText",
      attachments = Seq.empty)
    val emails = Mailer.sendEmails(email :: email.copy(recipients=List(Recipient(RecipientType.TO, EmailAddress("Jean Helou error receiver", "jean.helou+error@gmail.com")))) :: Nil)
    SentBatch(emails,inbox,errorInbox)
  }

  def withMessage[T](matchResult: Message => MatchResult[T]) =
    inbox.size must be_==(1).setMessage("No message available to test") and matchResult(inbox.get(0))

  "Mailer" should {

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

    "send multiple emails" in {
      batch.emails.map({
        case Success(e)=>e.subject
        case _=>"fail"}) ==="Test mail for batch"::"Test mail for batch"::Nil
      batch.inbox.size()===2
    }
    "send multiple emails handling errors" in {
      batchWithError.emails.map({
        case Success(e)=>e.subject
        case Failure=>"failed"
        case _ => "unexpected error"}) ==="Test mail for batch error"::"failed"::Nil
      batchWithError.inbox.size()===1
    }

    "send multiple emails with correct subject" in {
      batch.emails.map({
        case Success(e)=>e.subject
        case _=>"fail"}) ==="Test mail for batch"::"Test mail for batch"::Nil
      import scala.collection.JavaConversions._
      batch.inbox.map(_.getSubject) ==="Test mail for batch"::"Test mail for batch"::Nil
    }

  }
}