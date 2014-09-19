package play.modules.mailer

import scala.util.Failure
import scala.util.Try

import javax.mail.Transport

class Mailer(val session: Session) {

  def sendEmail(email: Email): Try[Unit] =
    tryWithTransport { implicit transport =>
      send(email)
    }.flatten.recoverWith {
      case TransportCloseException(result, cause) => 
        Failure(SendEmailTransportCloseException(result.asInstanceOf[Option[Try[Unit]]], cause))
      case cause: SendEmailException => Failure(cause)
      case cause => Failure(SendEmailException(email, cause))
    }

  def sendEmails(emails: Seq[Email]): Try[Seq[Try[Unit]]] =
    tryWithTransport { implicit transport =>
      emails.map(send)
    }.recoverWith {
      case TransportCloseException(results, cause) => 
        Failure(SendEmailsTransportCloseException(results.asInstanceOf[Option[Seq[Try[Unit]]]], cause))
      case cause => Failure(SendEmailsException(emails, cause))
    }

  private def tryWithTransport[T](code: Transport => T): Try[T] =
    Try {
      val transport = session.getTransport
      var result:Option[T] = None
      try {
        transport.connect()
        val codeResult = code(transport)
        result = Some(codeResult)
        codeResult
      } finally
        try {
          transport.close()
        } catch {
          case t: Throwable => throw TransportCloseException(result, t)
        }
    }

  private def send(email: Email)(implicit transport: Transport): Try[Unit] =
    Try {
      val message = email createFor session
      transport.sendMessage(message, message.getAllRecipients)
    }.recoverWith {
      case cause => Failure(SendEmailException(email, cause))
    }
}

case class SendEmailException(email: Email, cause: Throwable) extends RuntimeException(cause)
case class SendEmailsException(email: Seq[Email], cause: Throwable) extends RuntimeException(cause)
case class TransportCloseException[T](result:Option[T], cause: Throwable) extends RuntimeException(cause)
case class SendEmailTransportCloseException(result: Option[Try[Unit]], cause: Throwable) extends RuntimeException(cause)
case class SendEmailsTransportCloseException(results: Option[Seq[Try[Unit]]], cause: Throwable) extends RuntimeException(cause)
