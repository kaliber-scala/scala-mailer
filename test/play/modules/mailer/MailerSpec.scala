package play.modules.mailer

import org.specs2.mutable._
import play.api.test.FakeApplication
import javax.mail.Message
import org.jvnet.mock_javamail.Mailbox

class MailerSpec extends Specification with Before {
  lazy val configuration = Map (
      "mail.transport.protocol" -> "smtp"
    , "mail.smtp.host" -> "localhost"
    , "mail.smtp.port" -> "0"
    , "mail.smtp.failTo" -> "toto@localhost"
    , "mail.smtp.username" -> "foo"
    , "mail.smtp.password" -> "bar"
  )

  def f = FakeApplication(path=new java.io.File("./test/"),additionalConfiguration = configuration)
  
  def before = play.api.Play.start(f)
  "Ses" should {
    "send an email to ewestra@rhinofly.nl" in {
      val inbox = Mailbox.get("ewestra@rhinofly.nl");
      Mailer.sendEmail(Email(
        subject = "Test mail",
        from = EmailAddress("Erik Westra sender", "ewestra@rhinofly.nl"),
        replyTo = None,
        recipients = List(Recipient(Message.RecipientType.TO, EmailAddress("Erik Westra recipient", "ewestra@rhinofly.nl"))),
        text = "text",
        htmlText = "htmlText",
        attachments = Seq.empty))
      inbox.size()===1
    }
  }
}