package testUtils

import org.jvnet.mock_javamail.Mailbox
import play.api.test.FakeApplication
import play.api.test.WithApplication
import org.specs2.mutable.After

trait TestApplication extends After {

  lazy val configuration = Map(
    "mail.transport.protocol" -> "smtp",
    "mail.host" -> "localhost",
    "mail.port" -> "10000",
    "mail.failTo" -> "toto@localhost",
    "mail.username" -> "foo",
    "mail.password" -> "bar")

  private def f = FakeApplication(
    path = new java.io.File("./test/"),
    additionalConfiguration = configuration)

  abstract class TestApp extends WithApplication(f)

  def after = {
    Mailbox.clearAll()
  }
}