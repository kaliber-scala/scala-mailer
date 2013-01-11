package play.modules.mailer

import org.specs2.mutable._
import play.api.test.FakeApplication
import javax.mail.Message
import play.api.Configuration
import play.api.Mode

class MailerSpec extends Specification with Before {

  def f = FakeApplication(new java.io.File("./test/"))
  
  def before = play.api.Play.start(f)
  "Ses" should {
    "send an email to ewestra@rhinofly.nl" in {
      Mailer.sendEmail(Email(
        subject = "Test mail",
        from = EmailAddress("Erik Westra sender", "ewestra@rhinofly.nl"),
        replyTo = None,
        recipients = List(Recipient(Message.RecipientType.TO, EmailAddress("Erik Westra recipient", "ewestra@rhinofly.nl"))),
        text = "text",
        htmlText = "htmlText",
        attachments = Seq.empty))

      success
    }
  }
}