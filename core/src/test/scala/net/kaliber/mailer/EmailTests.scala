package net.kaliber.mailer

import org.specs2.mutable.Specification
import testUtils.{FullEmail, FullMessageTest, TestApplication, TestSettings}

object EmailTests extends Specification with TestApplication
  with FullEmail with FullMessageTest {

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

    "should have a convenience method for inline attachments" in {
      Attachment(name, byteArray, mimeType).inline === Attachment(name, byteArray, mimeType, Disposition.Inline)
    }
  }

  "Email" should {

    "have utility methods to easily create an htmlEmail" in {
      import fullEmailProperties._
      val email =
        Email(subject, EmailAddress(fromName, fromAddress), textContent)
          .withHtmlText(htmlTextContent)
          .to(toName, toAddress)
          .cc(ccName, ccAddress)
          .bcc(bccName, bccAddress)
          .replyTo(replyToName, replyToAddress)
          .withAttachments(
            Attachment(attachmentName, attachmentData, attachmentMimeType),
            Attachment(
              inlineAttachmentName,
              inlineAttachmentData,
              inlineAttachmentMimeType).inline)

      email === fullEmail
    }

    "have utility methods to easily create an textEmail" in {
      import fullEmailProperties._

      val email =
        Email(subject, EmailAddress(fromName, fromAddress), textContent, None)
          .to(toName, toAddress)
          .cc(ccName, ccAddress)
          .bcc(bccName, bccAddress)
          .replyTo(replyToName, replyToAddress)
          .withAttachments(
            Attachment(attachmentName, attachmentData, attachmentMimeType),
            Attachment(
              inlineAttachmentName,
              inlineAttachmentData,
              inlineAttachmentMimeType).inline)

      email === textEmail
    }

    "create a javax.mail.Message with the correct parts" in {
      val session = Session.fromSetting(TestSettings.mailerSettings)

      val fullMessage = fullEmail createFor session
      fullMessageTest(fullMessage)
    }
  }
}