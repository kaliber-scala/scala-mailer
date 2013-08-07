package play.modules.mailer

import org.specs2.mutable.Specification
import java.io.ByteArrayOutputStream
import javax.mail.Address
import javax.mail.Message
import javax.mail.internet.MimeMultipart
import javax.mail.internet.MimeBodyPart
import java.io.InputStream

object EmailTests extends Specification with TestApplication {

  "Attachment" should {

    val name = "testName"
    val mimeType = "test/mimeType"
    val byteArray = Array[Byte](1, 2)

    "have an alternative apply method for Array[Byte]" in {

      val attachment = Attachment(name, byteArray, mimeType)

      val inputStream = attachment.datasource.getInputStream
      val bytes = getByteArray(inputStream)

      attachment.name === (name)
      attachment.disposition === Disposition.Attachment
      attachment.datasource.getContentType === mimeType
      bytes === byteArray
    }

    "have an alternative apply method for Array[Byte] and custom disposition" in {

      val attachment = Attachment(name, byteArray, mimeType, Disposition.Inline)
      attachment.disposition === Disposition.Inline
    }

    "should throw an error if the mimetype does not contain a /" in {
      Attachment("", Array.empty[Byte], "mime") must throwAn[IllegalArgumentException]
      Attachment("", Array.empty[Byte], "/mime") must throwAn[IllegalArgumentException]
      Attachment("", Array.empty[Byte], "mime/") must throwAn[IllegalArgumentException]
    }
  }

  "Email" should {

    "create a javax.mail.Message with the correct parts" in {

      val email = Email(
        subject = "subject",
        from = EmailAddress(
          name = "from",
          address = "from@domain"),
        replyTo = Some(EmailAddress(
          name = "replyTo",
          address = "replyTo@domain")),
        recipients = Seq(
          Recipient(
            tpe = RecipientType.TO,
            emailAddress = EmailAddress("to", "to@domain")),
          Recipient(
            tpe = RecipientType.CC,
            emailAddress = EmailAddress("cc", "cc@domain")),
          Recipient(
            tpe = RecipientType.BCC,
            emailAddress = EmailAddress("bcc", "bcc@domain"))),
        text = "text",
        htmlText = "htmlText",
        attachments = Seq(
          Attachment(
            name = "attachment",
            data = Array[Byte](1, 2),
            mimeType = "attachment/type"),
          Attachment(
            name = "inline",
            data = Array[Byte](3, 4),
            mimeType = "inline/type",
            Disposition.Inline)))

      val session = Session.fromConfiguration

      val message = email createFor session

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
      
      message must beAnInstanceOf[javax.mail.Message]
      message.getSubject === "subject"
      recipients(message.getFrom) === Array("from <from@domain>")
      recipients(message.getReplyTo) === Array("replyTo <replyTo@domain>")
      recipients(message, RecipientType.TO) === Array("to <to@domain>")
      recipients(message, RecipientType.CC) === Array("cc <cc@domain>")
      recipients(message, RecipientType.BCC) === Array("bcc <bcc@domain>")

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
      textContent === "text"

      alternative.getBodyPart(1) must beAnInstanceOf[MimeBodyPart]
      val htmlPart = alternative.getBodyPart(1).asInstanceOf[MimeBodyPart]
      htmlPart.getContentType === "text/html; charset=UTF-8"
      val htmlContent = htmlPart.getContent
      htmlContent must beAnInstanceOf[String]
      htmlContent === "htmlText"

      related.getBodyPart(1) must beAnInstanceOf[MimeBodyPart]
      val inlineAttachment = related.getBodyPart(1).asInstanceOf[MimeBodyPart]
      inlineAttachment.getContentID === "<inline>"
      inlineAttachment.getContentType === "inline/type; filename=inline; name=inline"
      inlineAttachment.getDisposition must startWith("inline")
      val inlineAttachmentBytes = getByteArray(inlineAttachment.getDataHandler.getInputStream)
      inlineAttachmentBytes === Array(3, 4)
      
      root.getBodyPart(1) must beAnInstanceOf[MimeBodyPart]
      val attachment = root.getBodyPart(1).asInstanceOf[MimeBodyPart]
      attachment.getContentID === "<attachment>"
      attachment.getContentType === "attachment/type; filename=attachment; name=attachment"
      attachment.getDisposition must startWith("attachment")
      val attachmentBytes = getByteArray(attachment.getDataHandler.getInputStream)
      attachmentBytes === Array(1, 2)
    }
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