package play.modules.mailer

import play.api.PlayException
import play.api.Application

object PlayConfiguration {
  def apply(key:String, default:Option[String]=None)(implicit app:Application):String={
    app.configuration.getString(key).orElse(default)
       .getOrElse(throw new PlayException("Configuration error", "Could not find " + key + " in settings"))
  }
}
