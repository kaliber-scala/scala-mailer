package net.kaliber.mailer

import scala.util.Try

case class SendEmailException(email: Email, cause: Throwable) extends RuntimeException(cause)
case class SendEmailsException(email: Seq[Email], cause: Throwable) extends RuntimeException(cause)
case class TransportCloseException[T](result:Option[T], cause: Throwable) extends RuntimeException(cause)
case class SendEmailTransportCloseException(result: Option[Try[Unit]], cause: Throwable) extends RuntimeException(cause)
case class SendEmailsTransportCloseException(results: Option[Seq[Try[Unit]]], cause: Throwable) extends RuntimeException(cause)
