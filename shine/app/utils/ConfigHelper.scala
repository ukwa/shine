package utils

object ConfigHelper {

  /**
    * Returns the boolean values from the object keys in application.conf under shine { ... }
    *
    * @param tab
    * @return
    */
   def showOption(tab: String) = {
    play.api.Play.current.configuration.getConfig("shine") match {
      case Some(config) => {
        config.getBoolean(tab) match {
          case Some(option) => { option.asInstanceOf[Boolean] }
          case None => { false }
        }
      }
      case None => { false }
    }
  }
}