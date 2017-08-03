package profig

import scala.language.experimental.macros

/**
  * Platform-specific initialization for Scala.js
  */
object ProfigPlatform {
  /**
    * Called upon initialization of Config at first use. Currently does nothing for Scala.js.
    */
  def init(): Unit = macro PlatformMacros.init
}