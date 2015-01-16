package testUtils

import play.modules.mailer.Disposition
import play.modules.mailer.Email
import play.modules.mailer.EmailAddress
import play.modules.mailer.Recipient
import play.modules.mailer.RecipientType
import play.modules.mailer.Attachment

trait FullEmail {

  object fullEmailProperties {
    val subject = "subject"
    val fromName = "from"
    val fromAddress = "from@domain"
    val replyToName = "replyTo"
    val replyToAddress = "replyTo@domain"
    val toName = "to"
    val toAddress = "to@domain"
    val ccName = "cc"
    val ccAddress = "cc@domain"
    val bccName = "bcc"
    val bccAddress = "bcc@domain"
    val textContent = "text"
    val htmlTextContent = "htmlText"
    val attachmentName = "attachment"
    val attachmentData = Array[Byte](1, 2)
    val attachmentMimeType = "attachment/type"
    val inlineAttachmentName = "inline"
    val inlineAttachmentData = Array[Byte](3, 4)
    val inlineAttachmentMimeType = "inline/type"
  }

  val fullEmail = {
    import fullEmailProperties._

    Email(subject,
      EmailAddress(fromName, fromAddress),
      textContent,
      htmlTextContent,
      replyTo = Some(EmailAddress(replyToName, replyToAddress)),
      recipients = Seq(
        Recipient(
          tpe = RecipientType.TO,
          emailAddress = EmailAddress(toName, toAddress)),
        Recipient(
          tpe = RecipientType.CC,
          emailAddress = EmailAddress(ccName, ccAddress)),
        Recipient(
          tpe = RecipientType.BCC,
          emailAddress = EmailAddress(bccName, bccAddress))),
      attachments = Seq(
        Attachment(attachmentName, attachmentData, attachmentMimeType),
        Attachment(
          inlineAttachmentName, inlineAttachmentData, inlineAttachmentMimeType,
          Disposition.Inline)))
  }
  
  val textEmail = {
    import fullEmailProperties._

    Email(subject,
      EmailAddress(fromName, fromAddress),
      textContent,
      None,
      replyTo = Some(EmailAddress(replyToName, replyToAddress)),
      recipients = Seq(
        Recipient(
          tpe = RecipientType.TO,
          emailAddress = EmailAddress(toName, toAddress)),
        Recipient(
          tpe = RecipientType.CC,
          emailAddress = EmailAddress(ccName, ccAddress)),
        Recipient(
          tpe = RecipientType.BCC,
          emailAddress = EmailAddress(bccName, bccAddress))),
      attachments = Seq(
        Attachment(attachmentName, attachmentData, attachmentMimeType),
        Attachment(
          inlineAttachmentName, inlineAttachmentData, inlineAttachmentMimeType,
          Disposition.Inline)))
  }
}