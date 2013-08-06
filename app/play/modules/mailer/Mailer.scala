package play.modules.mailer

import scala.util.Failure
import scala.util.Try

import javax.mail.Transport
import play.api.Play.current

trait Mailer {

  private def send(email: Email)(implicit transport: Transport, session: Session): Try[Unit] =
    Try {
      val message = email createFor session
      transport.sendMessage(message, message.getAllRecipients)
    }.recoverWith {
      case cause => Failure(SendEmailException(email, cause))
    }

  private def tryWithTransport[T](code: Transport => T)(implicit session: Session): Try[T] = 
		  Try {
    val transport = session.getTransport
    try {
      transport.connect()
      code(transport)
    } finally {
      transport.close()
    }
  }

  def sendEmail(email: Email)(implicit session: Session = Session.fromConfiguration): Try[Unit] =
    tryWithTransport { implicit transport =>
      send(email)
    }.flatten.recoverWith {
      case cause: SendEmailException => Failure(cause)
      case cause => Failure(SendEmailException(email, cause))
    }

  def sendEmails(emails: Seq[Email])(implicit session: Session = Session.fromConfiguration): Try[Seq[Try[Unit]]] =
    tryWithTransport { implicit transport =>
      emails.map(send)
    }.recoverWith {
      case cause => Failure(SendEmailsException(emails, cause))
    }

  case class SendEmailException(email: Email, cause: Throwable) extends RuntimeException(cause)
  case class SendEmailsException(email: Seq[Email], cause: Throwable) extends RuntimeException(cause)

  object keys {
    lazy val protocol = PlayConfiguration("mail.transport.protocol", default = "smtps")
    lazy val sslEnable = PlayConfiguration("mail.smtp.ssl.enable", default = "true")
    lazy val host = PlayConfiguration("mail.smtp.host")
    lazy val port = PlayConfiguration("mail.smtp.port")
    lazy val username = PlayConfiguration("mail.smtp.username")
    lazy val password = PlayConfiguration("mail.smtp.password")
    lazy val failTo = PlayConfiguration("mail.smtp.failTo")
  }
}

object Mailer extends Mailer


