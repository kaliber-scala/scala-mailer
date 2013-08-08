package play.modules.mailer

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import scala.util.Try

class AsyncMailer(val mailer: Mailer) {

  def sendEmail(email: Email)(implicit executionContext: ExecutionContext): Future[Unit] = {
    Future(mailer.sendEmail(email)).flatMap {
      case Failure(t) => Future.failed(t)
      case Success(u) => Future.successful(u)
    }
  }

  def sendEmails(emails: Seq[Email])(implicit executionContext: ExecutionContext): Future[Seq[Try[Unit]]] = {
    Future(mailer.sendEmails(emails)).flatMap {
      case Failure(t) => Future.failed(t)
      case Success(u) => Future.successful(u)
    }
  }
}

object AsyncMailer extends AsyncMailer(Mailer)