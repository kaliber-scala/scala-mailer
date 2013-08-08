package play.modules.mailer

import org.jvnet.mock_javamail.Mailbox

trait MailboxUtilities { self: FullEmail =>
  
  val simpleEmail = {
    import fullEmailProperties._

    Email(
      subject,
      EmailAddress(fromName, fromAddress),
      textContent,
      htmlTextContent)
      .to(toName, toAddress)
  }

  val failName = "fail"
  val failAddress = "fail@domain"

  val simpleFailEmail =
    simpleEmail
      .copy(recipients = Seq.empty)
      .to(failName, failAddress)

  val simpleEmails =
    Seq(simpleEmail, simpleFailEmail)

  def withMailboxes[T](addresses: String*)(code: Seq[Mailbox] => T) = {
    val mailboxes = addresses.map(Mailbox.get)
    code(mailboxes)
    mailboxes.foreach(_.setError(false))
    mailboxes.foreach(_.clear())
  }

  def withDefaultMailbox[T](code: Mailbox => T) =
    withMailboxes(fullEmailProperties.toAddress) { mailboxes =>
      code(mailboxes.head)
    }

  def withFaultyMailbox[T](code: Mailbox => T) =
    withDefaultMailbox { mailbox =>
      mailbox.setError(true)
      code(mailbox)
    }
}