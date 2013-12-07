package play.modules.mailer

import org.specs2.mutable.Specification
import play.api.PlayException
import play.api.test.FakeApplication
import play.api.test.Helpers.running
import play.api.Play

object MailerBugsInIsolation extends Specification {

  "Mailer" should {

    "not give a `Could not initialize class play.modules.mailer.Mailer$` when called" in {
      running(FakeApplication()) {
        (Mailer must not(throwA[ExceptionInInitializerError])) and
          (Mailer must not(throwA[NoClassDefFoundError])) and
          (Mailer must throwA[PlayException])
      }
    }
  }
}