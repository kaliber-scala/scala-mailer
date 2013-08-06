package play.modules.mailer

import org.jvnet.mock_javamail.Mailbox
import play.api.test.FakeApplication

trait TestConfiguration {

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

  def testEmail(
    subject: String = "Test mail",
    from: EmailAddress = EmailAddress("Erik Westra sender", "ewestra@rhinofly.nl"),
    recipient: EmailAddress = EmailAddress("Erik Westra recipient", "ewestra@rhinofly.nl")) =
    Email(
      subject,
      from,
      replyTo = None,
      recipients = List(Recipient(RecipientType.TO, recipient)),
      text = "text",
      htmlText = "htmlText",
      attachments = Seq.empty)

  def defaultInbox =
    Mailbox.get("ewestra@rhinofly.nl")
}