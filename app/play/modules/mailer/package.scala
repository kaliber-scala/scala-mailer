package play.modules

package object mailer {
  type DataSource = javax.activation.DataSource
  type RecipientType = javax.mail.Message.RecipientType
  object RecipientType {
    val TO:RecipientType=javax.mail.Message.RecipientType.TO
    val BCC:RecipientType=javax.mail.Message.RecipientType.BCC
    val CC:RecipientType=javax.mail.Message.RecipientType.CC
  }
}
