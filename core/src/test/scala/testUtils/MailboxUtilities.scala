package testUtils

import org.jvnet.mock_javamail.Mailbox

import net.kaliber.mailer.Email
import net.kaliber.mailer.EmailAddress

trait MailboxUtilities { self: FullEmail =>

  val simpleEmail = {
    import fullEmailProperties._

    Email(
      subject,
      EmailAddress(fromName, fromAddress),
      textContent)
      .withHtmlText(htmlTextContent)
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

  def withMailboxes[T](addresses: String*)(code: Seq[Mailbox] => T):T = {
    val mailboxes = addresses.map(Mailbox.get)
    val result = code(mailboxes)
    mailboxes.foreach(_.setError(false))
    mailboxes.foreach(_.clear())
    result
  }

  def withDefaultMailbox[T](code: Mailbox => T):T =
    withMailboxes(fullEmailProperties.toAddress) { mailboxes =>
      code(mailboxes.head)
    }

  def withFaultyMailbox[T](code: Mailbox => T):T =
    withDefaultMailbox { mailbox =>
      mailbox.setError(true)
      code(mailbox)
    }
}
