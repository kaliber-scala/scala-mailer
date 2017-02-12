package net.kaliber.play

import net.kaliber.mailer.Session
import play.api.{Application, Configuration}

package object mailer {

  object Session {
    def fromApplication(implicit app: Application): Session = fromConfiguration(app.configuration)

    def fromConfiguration(configuration: Configuration): Session =
      net.kaliber.mailer.Session.fromConfig(configuration.underlying)
  }
}
