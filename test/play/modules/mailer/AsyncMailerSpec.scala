package play.modules.mailer

import org.jvnet.mock_javamail.Mailbox
import org.specs2.matcher.MatchResult
import org.specs2.mutable.{ BeforeAfter, Specification }
import javax.mail.internet.InternetAddress
import play.api.test.FakeApplication
import javax.mail.Message
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class AsyncMailerSpec extends Specification with BeforeAfter with TestConfiguration {

  implicit def c = play.api.libs.concurrent.Execution.Implicits.defaultContext

  "AsyncMailer" should {

    "should call sendMail" in {

      val email = testEmail()
      val result = AsyncMailer.sendEmail(email)

      Await.result(result, Duration.Inf)
      defaultInbox.size === 1
    }

  }
}