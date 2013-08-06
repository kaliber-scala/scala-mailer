package play.modules.mailer

import javax.mail.internet.MimeMessage
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.Part
import javax.mail.internet.MimeMultipart
import javax.mail.Message
import javax.mail.util.ByteArrayDataSource
import java.util.Date
import javax.activation.DataHandler
import scala.language.implicitConversions

case class Email(subject: String, from: EmailAddress, replyTo: Option[EmailAddress], recipients: Seq[Recipient], text: String, htmlText: String, attachments: Seq[Attachment]) {

  type Root = MimeMultipart
  type Related = MimeMultipart
  type Alternative = MimeMultipart

  def createFor(session: Session): Message = {

    val (root, related, alternative) = messageStructure

    val message = new MimeMessage(session)
    message setSubject (subject, "UTF-8")
    message setFrom from
    replyTo foreach (replyTo => message setReplyTo Array(replyTo))
    message setContent root
    message setSentDate new Date

    recipients foreach {
      case Recipient(tpe, emailAddress) =>
        message.addRecipient(tpe, emailAddress)
    }

    val messagePart = new MimeBodyPart
    messagePart setText (text, "UTF-8")
    alternative addBodyPart messagePart

    val messagePartHtml = new MimeBodyPart
    messagePartHtml setContent (htmlText, "text/html; charset=\"UTF-8\"")
    alternative addBodyPart messagePartHtml

    attachments foreach { 
      case a @ Attachment(_, _, Disposition.Inline) =>
        related addBodyPart a
      case a @ Attachment(_, _, Disposition.Attachment) =>
        root addBodyPart a
    }

    message.saveChanges()

    message
  }

  private[mailer] def messageStructure: (Root, Related, Alternative) = {
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
  
  private implicit def emailAddressToInternetAddress(emailAddress: EmailAddress): InternetAddress =
    new InternetAddress(emailAddress.address, emailAddress.name)

  private implicit def attachmentToMimeBodyPart(attachment: Attachment): MimeBodyPart = {
    val Attachment(name, datasource, disposition) = attachment

    val datasourceName = datasource.getName

    val attachmentPart = new MimeBodyPart
    attachmentPart.setDataHandler(new DataHandler(datasource))
    attachmentPart.setFileName(name)
    attachmentPart.setHeader("Content-Type", datasource.getContentType + "; filename=" + datasourceName + "; name=" + datasourceName)
    attachmentPart.setHeader("Content-ID", "<" + datasourceName + ">")
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

case class Attachment(name: String, datasource: DataSource, disposition: Disposition)

object Attachment extends Function3[String, DataSource, Disposition, Attachment] {
  def apply(name: String, data: Array[Byte], mimeType: String): Attachment = {
    val dataSource = new ByteArrayDataSource(data, mimeType)
    dataSource setName name
    apply(name, dataSource, Disposition.Attachment)
  }

  def apply(name: String, data: Array[Byte], mimeType: String, disposition: Disposition): Attachment = {
    val dataSource = new ByteArrayDataSource(data, mimeType)
    dataSource setName name
    apply(name, dataSource, disposition)
  }
}