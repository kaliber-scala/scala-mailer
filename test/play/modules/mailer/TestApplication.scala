package play.modules.mailer

import play.api.test.FakeApplication
import org.jvnet.mock_javamail.Mailbox
import org.specs2.mutable.BeforeAfter

trait TestApplication extends BeforeAfter {

  lazy val configuration = Map(
    "mail.transport.protocol" -> "smtp",
    "mail.smtp.host" -> "localhost",
    "mail.smtp.port" -> "10000",
    "mail.smtp.failTo" -> "toto@localhost",
    "mail.smtp.username" -> "foo",
    "mail.smtp.password" -> "bar")

  def f = FakeApplication(
    path = new java.io.File("./test/"),
    additionalConfiguration = configuration)

  def before = play.api.Play.start(f)
  
  def after = Mailbox.clearAll()
}