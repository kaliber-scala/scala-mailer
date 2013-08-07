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

object MailerTests extends Specification with TestApplication {

  "Mailer" should {

    "have the correct default instance" in {
      Mailer must beAnInstanceOf[Mailer]
      Mailer.session === Session.fromConfiguration
    }

    "have a method sendEmail" >> {

      "that fails correctly if no provider can be found" in {

        val session = javax.mail.Session.getInstance(new Properties())

        val mailer = new Mailer(session)

        mailer.sendEmail(simpleEmail) must beLike {
          case Failure(Mailer.SendEmailException(email, t)) =>
            email === simpleEmail
            t must beAnInstanceOf[NoSuchProviderException]
        }
      }

      "that fails correctly if the connection fails" in {
        val provider =
          new Provider(Type.TRANSPORT, "testProtocol", classOf[FaultyConnectionTransport].getName, "Test", "0.1-SNAPSHOT")

        val properties = new Properties()
        properties.put("mail.transport.protocol", "testProtocol")

        val session = javax.mail.Session.getInstance(properties)
        session.addProvider(provider)

        val mailer = new Mailer(session)
        mailer.sendEmail(simpleEmail) must beLike {
          case Failure(Mailer.SendEmailException(email, t)) =>
            email === simpleEmail
            t must beAnInstanceOf[MessagingException]
            t.getMessage === "connectionFailed"
        }

      }
      
      "that fails correctly if sending the message fails" in {
        val provider =
          new Provider(Type.TRANSPORT, "testProtocol", classOf[FaultyMessageTransport].getName, "Test", "0.1-SNAPSHOT")

        val properties = new Properties()
        properties.put("mail.transport.protocol", "testProtocol")

        val session = javax.mail.Session.getInstance(properties)
        session.addProvider(provider)

        val mailer = new Mailer(session)
        mailer.sendEmail(simpleEmail) must beLike {
          case Failure(Mailer.SendEmailException(email, t)) =>
            email === simpleEmail
            t must beAnInstanceOf[MessagingException]
            t.getMessage === "sendMessageFailed"
        }

      }
    }

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

  val simpleEmail =
    Email(
      subject = "subject",
      from = EmailAddress("from", "from@domain"),
      text = "text",
      htmlText = "htmlText")
      .to("to", "to@domain")

}