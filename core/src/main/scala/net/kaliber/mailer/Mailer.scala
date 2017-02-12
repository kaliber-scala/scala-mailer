package net.kaliber.mailer

import javax.mail.Transport
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try

class Mailer(session: Session) {

  private lazy val mailer = new Mailer.Synchronous(session)

  def sendEmail(email: Email)(implicit ec: ExecutionContext): Future[Unit] =
    Future(mailer sendEmail email).flatMap {
      case Failure(t) => Future.failed(t)
      case Success(u) => Future.successful(u)
    }

  def sendEmails(emails: Seq[Email])(implicit ec: ExecutionContext): Future[Seq[Try[Unit]]] =
    Future(mailer sendEmails emails).flatMap {
      case Failure(t) => Future.failed(t)
      case Success(u) => Future.successful(u)
    }
}

object Mailer {

  /**
   * The Java mailing utilities are synchronous. In order to keep the code readable
   * the synchronous version is a separate class.
   */
  private class Synchronous(session: Session) {

    def sendEmail(email: Email): Try[Unit] =
      tryWithTransport { implicit transport =>
        send(email)
      }
      .flatten
      .recoverWith {
        case TransportCloseException(result, cause) =>
          Failure(SendEmailTransportCloseException(result.asInstanceOf[Option[Try[Unit]]], cause))

        case cause: SendEmailException =>
          Failure(cause)

        case cause =>
          Failure(SendEmailException(email, cause))
      }

    def sendEmails(emails: Seq[Email]): Try[Seq[Try[Unit]]] =
      tryWithTransport { implicit transport =>
        emails.map(send)
      }
      .recoverWith {
        case TransportCloseException(results, cause) =>
          Failure(SendEmailsTransportCloseException(results.asInstanceOf[Option[Seq[Try[Unit]]]], cause))

        case cause =>
          Failure(SendEmailsException(emails, cause))
      }

    private def tryWithTransport[T](code: Transport => T): Try[T] =
      for {
        transport      <- Try(session.getTransport)
        _              =  transport.connect()
        // save the try instead of extracting the value to make sure we can close the transport
        possibleResult =  Try(code(transport))
        _              <- Try(transport.close()) recoverWith createTransportCloseException(possibleResult)
        result         <- possibleResult
      } yield result

    private def createTransportCloseException[T](result: Try[T]): PartialFunction[Throwable, Try[TransportCloseException[T]]] = {
      case t: Throwable => Failure(TransportCloseException(result.toOption, t))
    }

    private def send(email: Email)(implicit transport: Transport): Try[Unit] =
      Try {
        val message = email createFor session
        transport.sendMessage(message, message.getAllRecipients)
      }.recoverWith {
        case cause => Failure(SendEmailException(email, cause))
      }
  }
}
