package play.modules.mailer

import java.util.Date

import javax.activation.DataHandler
import javax.mail.Message
import javax.mail.Part
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

case class Email(subject: String, from: EmailAddress, text: String, htmlText: String, replyTo: Option[EmailAddress] = None, recipients: Seq[Recipient] = Seq.empty, attachments: Seq[Attachment] = Seq.empty) {

  def to(name: String, address: String) =
    copy(recipients = recipients :+
      Recipient(RecipientType.TO, EmailAddress(name, address)))

  def cc(name: String, address: String) =
    copy(recipients = recipients :+
      Recipient(RecipientType.CC, EmailAddress(name, address)))

  def bcc(name: String, address: String) =
    copy(recipients = recipients :+
      Recipient(RecipientType.BCC, EmailAddress(name, address)))

  def replyTo(name: String, address: String) =
    copy(replyTo = Some(EmailAddress(name, address)))

  def withAttachments(attachments: Attachment*) =
    copy(attachments = this.attachments ++ attachments)

  def createFor(session: Session): Message = {

    val (root, related, alternative) = messageStructure

    val message = createMimeMessage(session, root)
    addRecipients(message)
    addTextPart(alternative)
    addHtmlPart(alternative)
    addAttachments(root, related)

    message.saveChanges()

    message
  }

  private type Root = MimeMultipart
  private type Related = MimeMultipart
  private type Alternative = MimeMultipart

  private def messageStructure: (Root, Related, Alternative) = {
    val root = new MimeMultipart("mixed")
    val relatedPart = new MimeBodyPart
    val related = new MimeMultipart("related")

    root addBodyPart relatedPart
    relatedPart setContent related

    val alternativePart = new MimeBodyPart
    val alternative = new MimeMultipart("alternative")

    related addBodyPart alternativePart
    alternativePart setContent alternative

    (root, related, alternative)
  }

  private def createMimeMessage(session: play.modules.mailer.Session, root: Email.this.Root): javax.mail.internet.MimeMessage = {

    val message = new MimeMessage(session)
    message setSubject (subject, "UTF-8")
    message setFrom from
    replyTo foreach (replyTo => message setReplyTo Array(replyTo))
    message setContent root
    message setSentDate new Date
    message
  }

  private def addRecipients(message: javax.mail.internet.MimeMessage): Unit =
    recipients foreach {
      case Recipient(tpe, emailAddress) =>
        message addRecipient (tpe, emailAddress)
    }

  private def addTextPart(alternative: Alternative): Unit = {
    val messagePart = new MimeBodyPart
    messagePart setText (text, "UTF-8")
    alternative addBodyPart messagePart
  }

  private def addHtmlPart(alternative: Alternative): Unit = {
    val messagePartHtml = new MimeBodyPart
    messagePartHtml setContent (htmlText, "text/html; charset=UTF-8")
    alternative addBodyPart messagePartHtml
  }

  private def addAttachments(root: Root, related: Related): Unit =
    attachments foreach {
      case a @ Attachment(_, _, Disposition.Inline) =>
        related addBodyPart a
      case a @ Attachment(_, _, Disposition.Attachment) =>
        root addBodyPart a
    }

  import scala.language.implicitConversions
  
  private implicit def emailAddressToInternetAddress(emailAddress: EmailAddress): InternetAddress =
    new InternetAddress(emailAddress.address, emailAddress.name)

  private implicit def attachmentToMimeBodyPart(attachment: Attachment): MimeBodyPart = {
    val Attachment(name, datasource, disposition) = attachment

    val datasourceName = datasource.getName

    val attachmentPart = new MimeBodyPart
    attachmentPart.setDataHandler(new DataHandler(datasource))
    attachmentPart.setFileName(name)
    attachmentPart.setHeader("Content-Type", datasource.getContentType + "; filename=" + datasourceName + "; name=" + datasourceName)
    attachmentPart.setContentID("<" + datasourceName + ">")
    attachmentPart.setDisposition(disposition.value + "; size=0")

    attachmentPart
  }
}

case class EmailAddress(name: String, address: String)
case class Recipient(tpe: RecipientType, emailAddress: EmailAddress)

abstract sealed class Disposition(val value: String)
object Disposition {
  case object Inline extends Disposition(Part.INLINE)
  case object Attachment extends Disposition(Part.ATTACHMENT)
}

case class Attachment(name: String, datasource: DataSource, disposition: Disposition) {
  def inline = copy(disposition = Disposition.Inline)
}

case class ByteArrayDataSource(data: Array[Byte], mimeType: String)
  extends javax.mail.util.ByteArrayDataSource(data, mimeType)

object Attachment extends ((String, DataSource, Disposition) => Attachment) {

  def apply(name: String, data: Array[Byte], mimeType: String): Attachment =
    apply(name, data, mimeType, Disposition.Attachment)

  def apply(name: String, data: Array[Byte], mimeType: String, disposition: Disposition): Attachment = {
    require(mimeType matches ".+/.+", "Invalid MIME type, should contain a /")

    val dataSource = ByteArrayDataSource(data, mimeType)
    dataSource setName name
    apply(name, dataSource, disposition)
  }
}