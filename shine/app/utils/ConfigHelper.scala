package utils

import com.google.inject.Inject

class ConfigHelper @Inject() (configuration: play.api.Configuration) {

  /**
    * Returns the boolean values from the object keys in application.conf under shine { ... }
    *
    * @param tab
    * @return
    */
   def showOption(tab: String) = {
    configuration.getConfig("shine") match {
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