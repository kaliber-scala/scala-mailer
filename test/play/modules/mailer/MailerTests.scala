package play.modules.mailer

import org.specs2.mutable.Specification
import javax.mail.Provider
import java.util.Properties
import javax.mail.Provider.Type
import scala.util.Failure
import javax.mail.Transport
import javax.mail.Message
import javax.mail.Address
import javax.mail.MessagingException
import javax.mail.URLName
import javax.mail.NoSuchProviderException
import javax.mail.PasswordAuthentication
import javax.mail.Authenticator
import org.jvnet.mock_javamail.Mailbox
import scala.util.Success

object MailerTests extends Specification with TestApplication with FullEmail with FullMessageTest {

  "Mailer" should {

    "have the correct default instance" in {
      Mailer must beAnInstanceOf[Mailer]
      Mailer.session === Session.fromConfiguration
    }

    "have a method sendEmail that" >> {

      "fails correctly if no provider can be found" in {

        val session = javax.mail.Session.getInstance(new Properties())
        val result = sendMail(session)

        result must beLike {
          case Failure(SendEmailException(email, t)) =>
            email === simpleEmail
            t must beAnInstanceOf[NoSuchProviderException]
        }
      }

      "fails correctly if the connection fails" in {

        val result = sendMail(session(classOf[FaultyConnectionTransport]))
        result must beLike {
          case Failure(SendEmailException(email, t)) =>
            email === simpleEmail
            messagingExceptionWithMessage(t, "connectionFailed")
        }

      }

      "fails correctly if sending the message fails" in {

        val result = sendMail(session(classOf[FaultyMessageTransport]))
        result must beLike {
          case Failure(SendEmailException(email, t)) =>
            email === simpleEmail
            messagingExceptionWithMessage(t, "sendMessageFailed")
        }
      }

      "fails correctly if closing the transport fails" in {

        val result = sendMail(session(classOf[FaultyCloseTransport]))
        result must beLike {
          case Failure(TransportCloseException(t)) =>
            messagingExceptionWithMessage(t, "closeFailed")
        }
      }
      
      "fails if the mailbox responds in failure" in {
        
        withFaultyMailbox { mailbox =>
          
          val result = sendMail()
          
          result must beLike {
            case Failure(SendEmailException(email, t)) =>
              email === simpleEmail
              messagingExceptionWithMessage(t, s"Simulated error sending message to $toName <$toAddress>")
          }
        }
      }
      
      "correctly sends a full email" in {
        withDefaultMailbox { mailbox =>
          
          val result = sendMail(email = fullEmail)
          
          result === Success()
          mailbox.size === 1
          fullMessageTest(mailbox.get(0))
        }
      }
    }
  }

  def sendMail(session: Session = Session.fromConfiguration, email:Email = simpleEmail) = {
    val mailer = new Mailer(session)
    mailer.sendEmail(email)
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

  val toName = "to"
  val toAddress = "to@domain"
  
  val simpleEmail =
    Email(
      subject = "subject",
      from = EmailAddress("from", "from@domain"),
      text = "text",
      htmlText = "htmlText")
      .to(toName, toAddress)

  def withDefaultMailbox[T](code: Mailbox => T) = {
    val defaultMailbox = Mailbox.get(toAddress)
    code(defaultMailbox)
    defaultMailbox.setError(false)
    defaultMailbox.clear()
  }

  def withFaultyMailbox[T](code: Mailbox => T) =
    withDefaultMailbox { mailbox =>
      mailbox.setError(true)
      code(mailbox)
    }

}