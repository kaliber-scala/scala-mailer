package play.modules.mailer

import org.specs2.mutable.Before
import play.api.test.FakeApplication

trait TestApplication extends Before {

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
  
}