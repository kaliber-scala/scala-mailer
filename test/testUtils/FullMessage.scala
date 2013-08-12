package testUtils

import org.specs2.mutable.Specification
import javax.mail.Message
import javax.mail.internet.MimeBodyPart
import java.io.InputStream
import javax.mail.internet.MimeMultipart
import javax.mail.Address
import scala.Array.canBuildFrom
import javax.mail.Message.RecipientType

trait FullMessageTest { self: Specification with FullEmail =>

  def fullMessageTest(message: Message) = {

    /*
      	Message
      	|
      	|_ MimeMultipart - multipart/mixed
      	  |
      	  |
      	  |_ MimeBodyPart
      	  | |
      	  | |_ MimeMultipart - multipart/related
      	  |   |
      	  |   |_ MimeBodyPart
      	  |   | |
      	  |   | |_ MimeMultipart - multipart/alternative
      	  |   |   |
      	  |   |   |_ MimeBodyPart - text/plain
      	  |   |   |_ MimeBodyPart - text/html
      	  |   |
      	  |   |_ MimeBodyPart - inline/type
      	  |
      	  |_ MimeBodyPart - attachment/type   
       */

    import fullEmailProperties._

    message must beAnInstanceOf[javax.mail.Message]
    message.getSubject === subject
    recipients(message.getFrom) === Array(s"$fromName <$fromAddress>")
    recipients(message.getReplyTo) === Array(s"$replyToName <$replyToAddress>")
    recipients(message, RecipientType.TO) === Array(s"$toName <$toAddress>")
    recipients(message, RecipientType.CC) === Array(s"$ccName <$ccAddress>")
    recipients(message, RecipientType.BCC) === Array(s"$bccName <$bccAddress>")

    message.getContent must beAnInstanceOf[MimeMultipart]
    val root = message.getContent.asInstanceOf[MimeMultipart]
    root.getContentType must startWith("multipart/mixed;")
    root.getCount === 2

    root.getBodyPart(0) must beAnInstanceOf[MimeBodyPart]
    val relatedPart = root.getBodyPart(0).asInstanceOf[MimeBodyPart]

    relatedPart.getContent must beAnInstanceOf[MimeMultipart]
    val related = relatedPart.getContent.asInstanceOf[MimeMultipart]
    related.getContentType must startWith("multipart/related;")
    related.getCount === 2

    related.getBodyPart(0) must beAnInstanceOf[MimeBodyPart]
    val alternativePart = related.getBodyPart(0).asInstanceOf[MimeBodyPart]

    alternativePart.getContent must beAnInstanceOf[MimeMultipart]
    val alternative = alternativePart.getContent.asInstanceOf[MimeMultipart]
    alternative.getContentType must startWith("multipart/alternative;")
    alternative.getCount === 2

    alternative.getBodyPart(0) must beAnInstanceOf[MimeBodyPart]
    val textPart = alternative.getBodyPart(0).asInstanceOf[MimeBodyPart]
    textPart.getContentType === "text/plain; charset=UTF-8"
    val textContent = textPart.getContent
    textContent must beAnInstanceOf[String]
    textContent === textContent

    alternative.getBodyPart(1) must beAnInstanceOf[MimeBodyPart]
    val htmlPart = alternative.getBodyPart(1).asInstanceOf[MimeBodyPart]
    htmlPart.getContentType === "text/html; charset=UTF-8"
    val htmlContent = htmlPart.getContent
    htmlContent must beAnInstanceOf[String]
    htmlContent === htmlTextContent

    related.getBodyPart(1) must beAnInstanceOf[MimeBodyPart]
    val inlineAttachment = related.getBodyPart(1).asInstanceOf[MimeBodyPart]
    inlineAttachment.getContentID === s"<$inlineAttachmentName>"
    inlineAttachment.getContentType === s"$inlineAttachmentMimeType; filename=$inlineAttachmentName; name=$inlineAttachmentName"
    inlineAttachment.getDisposition must startWith("inline")
    val inlineAttachmentBytes = getByteArray(inlineAttachment.getDataHandler.getInputStream)
    inlineAttachmentBytes === inlineAttachmentData

    root.getBodyPart(1) must beAnInstanceOf[MimeBodyPart]
    val attachment = root.getBodyPart(1).asInstanceOf[MimeBodyPart]
    attachment.getContentID === s"<$attachmentName>"
    attachment.getContentType === s"$attachmentMimeType; filename=$attachmentName; name=$attachmentName"
    attachment.getDisposition must startWith("attachment")
    val attachmentBytes = getByteArray(attachment.getDataHandler.getInputStream)
    attachmentBytes === attachmentData

  }

  def getByteArray(inputStream: InputStream) =
    Stream
      .continually(inputStream.read)
      .takeWhile(_ != -1)
      .map(_.toByte)
      .toArray

  def recipients(r: Array[Address]): Array[String] =
    r.map(_.toString)

  def recipients(message: Message, tpe: RecipientType): Array[String] =
    recipients(message.getRecipients(tpe))

}