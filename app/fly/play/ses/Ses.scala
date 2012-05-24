package fly.play.ses

import fly.play.libraryUtils.PlayConfiguration
import play.api.Play.current
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.activation.DataSource
import javax.mail.util.ByteArrayDataSource
import javax.activation.DataHandler
import javax.mail.Part
import javax.mail.Session
import javax.mail.Message
import java.util.Date
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.PasswordAuthentication

object Ses {

  def session: Session = {
    val properties = new Properties()
    properties.put("mail.transport.protocol", "smtps")
    properties.put("mail.smtps.quitwait", "false")
    properties.put("mail.smtps.host", keys.host)
    properties.put("mail.smtps.port", keys.port)
    properties.put("mail.smtp.ssl.enable", "true")
    properties.put("mail.smtp.from", keys.failTo)
    
    val username = keys.username
    val password = keys.password

    properties.put("mail.smtps.username", username)
    properties.put("mail.smtps.auth", "true")

    Session.getInstance(properties, new Authenticator {

      override def getPasswordAuthentication = new PasswordAuthentication(username, password)

    })
  }

  def sendEmail(email: Email) = {

    val message = email createFor session
    
    val transport = session.getTransport();
    transport.connect
    transport.sendMessage(message, message.getAllRecipients)
    transport.close
  }

  object keys {
    lazy val host = PlayConfiguration("mail.smtp.host")
    lazy val port = PlayConfiguration("mail.smtp.port")
    lazy val username = PlayConfiguration("mail.smtp.username")
    lazy val password = PlayConfiguration("mail.smtp.password")
    lazy val failTo = PlayConfiguration("mail.smtp.failTo")
  }
}

case class Email(subject: String, from: EmailAddress, replyTo:Option[EmailAddress], recipients: Seq[Recipient], text: String, htmlText: String, attachments: Seq[Attachment]) {

  type Root = MimeMultipart
  type Related = MimeMultipart
  type Alternative = MimeMultipart
  
  def messageStructure:(Root, Related, Alternative) = {
    val root = new MimeMultipart("mixed")
    val relatedPart = new MimeBodyPart
    val related = new MimeMultipart("related")
    val alternativePart = new MimeBodyPart
    val alternative = new MimeMultipart("alternative")

    root addBodyPart relatedPart
    relatedPart setContent related

    related addBodyPart alternativePart
    alternativePart setContent alternative
    
    (root, related, alternative)
  }
  
  def createFor(session: Session): Message = {

	val (root, related, alternative) = messageStructure
    

    val message = new MimeMessage(session)
    message setSubject subject
    message setFrom from
    replyTo foreach(replyTo => message setReplyTo Array(replyTo))
    message setContent root
    message setSentDate new Date

    recipients foreach { r =>
      message.addRecipient(r.tpe, r.emailAddress)
    }
    
    val messagePart = new MimeBodyPart
    messagePart.setText(text, "UTF-8")
    alternative addBodyPart messagePart
    
    val messagePartHtml = new MimeBodyPart
    messagePartHtml.setContent(htmlText, "text/html; charset=\"UTF-8\"");
    alternative addBodyPart messagePartHtml

    attachments foreach { a =>
      a.disposition match {
        case Disposition.Inline => related addBodyPart a
        case Disposition.Attachment => root addBodyPart a
      }
    }

    message.saveChanges

    message
  }

  implicit def emailAddressToInternetAddress(emailAddress: EmailAddress): InternetAddress =
    new InternetAddress(emailAddress.address, emailAddress.name)

  implicit def attachmentToMimeBodyPart(attachment: Attachment): MimeBodyPart = {
    val Attachment(name, datasource, disposition) = attachment

    val datasourceName = datasource.getName

    val attachmentPart = new MimeBodyPart
    attachmentPart.setDataHandler(new DataHandler(datasource))
    attachmentPart.setFileName(name)
    attachmentPart.setHeader("Content-Type", datasource.getContentType + "; filename=" + datasourceName + "; name=" + datasourceName)
    attachmentPart.setHeader("Content-ID", "<" + datasourceName + ">")
    attachmentPart.setDisposition(disposition.value + "; size=0");

    attachmentPart
  }

}
case class EmailAddress(name: String, address: String)
case class Recipient(tpe: Message.RecipientType, emailAddress: EmailAddress)
case class Attachment(name: String, datasource: DataSource, disposition: Disposition)

abstract sealed class Disposition(val value: String)
object Disposition {
  case object Inline extends Disposition(Part.INLINE)
  case object Attachment extends Disposition(Part.ATTACHMENT)
}

object Attachment extends Function3[String, DataSource, Disposition, Attachment] {
  def apply(name: String, data: Array[Byte], mimeType: String): Attachment = {
    val dataSource = new ByteArrayDataSource(data, mimeType)
    dataSource setName name
    apply(name, dataSource, Disposition.Attachment)
  }
  
  def apply(name: String, data: Array[Byte], mimeType: String, disposition:Disposition): Attachment = {
		  val dataSource = new ByteArrayDataSource(data, mimeType)
		  dataSource setName name
		  apply(name, dataSource, disposition)
  }
}