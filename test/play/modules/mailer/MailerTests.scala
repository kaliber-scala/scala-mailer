package play.modules.mailer

import java.util.Properties

import scala.util.Failure
import scala.util.Success

import org.specs2.mutable.Specification

import javax.mail.Address
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.NoSuchProviderException
import javax.mail.Provider
import javax.mail.Provider.Type
import javax.mail.Transport
import javax.mail.URLName
import play.api.Play.current
import testUtils.FullEmail
import testUtils.FullMessageTest
import testUtils.MailboxUtilities
import testUtils.TestApplication

object MailerTests extends Specification with TestApplication
  with FullEmail with FullMessageTest with MailboxUtilities {

  "Mailer" should {

    "have the correct default instance" in new TestApp {
      Mailer must beAnInstanceOf[Mailer]
      Mailer.session must not beNull
    }

    import fullEmailProperties._

    val providerFailure = "fails correctly if no provider can be found"
    val connectionFailure = "fails correctly if the connection fails"
    val messageFailure = "fails correctly if sending the message fails"
    val closeFailure = "fails correctly if closing the transport fails"
    def simulatedErrorMessage(name: String, address: String) = s"Simulated error sending message to $name <$address>"

    "have a method sendEmail that" >> {

      providerFailure in {

        val session = javax.mail.Session.getInstance(new Properties())
        val result = sendMail(session)

        result must beLike {
          case Failure(SendEmailException(email, t)) =>
            email === simpleEmail
            t must beAnInstanceOf[NoSuchProviderException]
        }
      }

      connectionFailure in {

        val result = sendMail(session(classOf[FaultyConnectionTransport]))
        result must beLike {
          case Failure(SendEmailException(email, t)) =>
            email === simpleEmail
            messagingExceptionWithMessage(t, "connectionFailed")
        }

      }

      messageFailure in {

        val result = sendMail(session(classOf[FaultyMessageTransport]))
        result must beLike {
          case Failure(SendEmailException(email, t)) =>
            email === simpleEmail
            messagingExceptionWithMessage(t, "sendMessageFailed")
        }
      }

      closeFailure in {

        val result = sendMail(session(classOf[FaultyCloseTransport]))
        result must beLike {
          case Failure(SendEmailTransportCloseException(result, t)) =>
            result === Some(Success())
            messagingExceptionWithMessage(t, "closeFailed")
        }
      }

      "fails if the mailbox responds in failure" in new TestApp {

        withFaultyMailbox { mailbox =>

          val result = sendMail()

          result must beLike {
            case Failure(SendEmailException(email, t)) =>
              email === simpleEmail
              messagingExceptionWithMessage(t, simulatedErrorMessage(toName, toAddress))
          }
        }
      }

      "correctly sends a full email" in new TestApp {

        withDefaultMailbox { mailbox =>

          val result = sendMail(email = fullEmail)

          result === Success()
          mailbox.size === 1
          fullMessageTest(mailbox.get(0))
        }
      }
    }

    "have a method sendEmails that" >> {

      providerFailure in {

        val session = javax.mail.Session.getInstance(new Properties())
        val result = sendMails(session)

        result must beLike {
          case Failure(SendEmailsException(emails, t)) =>
            emails === simpleEmails
            t must beAnInstanceOf[NoSuchProviderException]
        }
      }

      connectionFailure in {

        val result = sendMails(session(classOf[FaultyConnectionTransport]))
        result must beLike {
          case Failure(SendEmailsException(emails, t)) =>
            emails === simpleEmails
            messagingExceptionWithMessage(t, "connectionFailed")
        }

      }

      messageFailure in {

        val result = sendMails(session(classOf[FaultyMessageTransport]))
        result must beLike {
          case Success(Seq(
            Failure(SendEmailException(email1, t1)),
            Failure(SendEmailException(email2, t2)))) =>
            email1 === simpleEmail
            email2 === simpleFailEmail
            messagingExceptionWithMessage(t1, "sendMessageFailed")
            messagingExceptionWithMessage(t2, "sendMessageFailed")
        }
      }

      closeFailure in {

        val result = sendMails(session(classOf[FaultyCloseTransport]))
        result must beLike {
          case Failure(SendEmailsTransportCloseException(results, t)) =>
            results === Some(Seq(Success(), Success()))
            messagingExceptionWithMessage(t, "closeFailed")
        }
      }

      "partially fails if the mailbox responds in failure for one of the emails" in new TestApp {

        withMailboxes(toAddress, failAddress) { mailboxes =>

          mailboxes.last.setError(true)

          val result = sendMails()

          result must beLike {
            case Success(Seq(Success(_), Failure(SendEmailException(email, t)))) =>
              email === simpleFailEmail
              messagingExceptionWithMessage(t, simulatedErrorMessage(failName, failAddress))
          }

          mailboxes.head.size === 1
        }
      }

      "correctly sends 2 emails" in new TestApp {

        withDefaultMailbox { mailbox =>

          val result = sendMails(emails = Seq(simpleEmail, simpleEmail))

          result === Success(Seq(Success(), Success()))
          mailbox.size === 2
        }
      }
    }
  }

  def sendMail(session: Session = Session.fromConfiguration, email: Email = simpleEmail) = {
    val mailer = new Mailer(session)
    mailer.sendEmail(email)
  }

  def sendMails(session: Session = Session.fromConfiguration, emails: Seq[Email] = simpleEmails) = {
    val mailer = new Mailer(session)
    mailer.sendEmails(emails)
  }

  def messagingExceptionWithMessage(t: Throwable, message: String) = {
    t must beAnInstanceOf[MessagingException]
    t.getMessage === message
  }

  def session[T](tpe: Class[T]) = {
    val provider =
      new Provider(Type.TRANSPORT, "testProtocol", tpe.getName, "Test", "0.1-SNAPSHOT")

    val properties = new Properties()
    properties.put("mail.transport.protocol", "testProtocol")

    val session = javax.mail.Session.getInstance(properties)
    session.addProvider(provider)
    session
  }

  class FaultyConnectionTransport(session: Session, urlname: URLName) extends Transport(session, urlname) {

    def sendMessage(msg: Message, addresses: Array[Address]) =
      ???

    override protected def protocolConnect(host: String, port: Int, user: String, password: String): Boolean =
      throw new MessagingException("connectionFailed")
  }

  class FaultyMessageTransport(session: Session, urlname: URLName) extends Transport(session, urlname) {

    def sendMessage(msg: Message, addresses: Array[Address]) =
      throw new MessagingException("sendMessageFailed")

    override protected def protocolConnect(host: String, port: Int, user: String, password: String): Boolean =
      true
  }

  class FaultyCloseTransport(session: Session, urlname: URLName) extends Transport(session, urlname) {

    def sendMessage(msg: Message, addresses: Array[Address]) =
      {}

    override protected def protocolConnect(host: String, port: Int, user: String, password: String): Boolean =
      true

    override def close() =
      throw new MessagingException("closeFailed")
  }
}