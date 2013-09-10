package testUtils

import org.jvnet.mock_javamail.Mailbox
import org.specs2.mutable.BeforeAfter

import play.api.test.FakeApplication

trait TestApplication extends BeforeAfter {

  lazy val configuration = Map(
    "mail.transport.protocol" -> "smtp",
    "mail.host" -> "localhost",
    "mail.port" -> "10000",
    "mail.failTo" -> "toto@localhost",
    "mail.username" -> "foo",
    "mail.password" -> "bar")

  def f = FakeApplication(
    path = new java.io.File("./test/"),
    additionalConfiguration = configuration)

  def before = play.api.Play.start(f)
  
  def after = {
    Mailbox.clearAll()
  }
}