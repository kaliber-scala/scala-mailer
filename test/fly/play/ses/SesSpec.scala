package fly.play.ses

import org.specs2.mutable._
import play.api.test.FakeApplication
import javax.mail.Message

class SesSpec extends Specification with Before {

  def before = play.api.Play.start(FakeApplication())
  "Ses" should {
    "send an email to ewestra@rhinofly.nl" in {
      Ses.sendEmail(Email(
        subject = "Test mail",
        from = EmailAddress("Erik Westra sender", "ewestra@rhinofly.nl"),
        recipients = List(Recipient(Message.RecipientType.TO, EmailAddress("Erik Westra recipient", "ewestra@rhinofly.nl"))),
        text = "text",
        htmlText = "htmlText",
        attachments = Seq.empty))

      success
    }
  }
}