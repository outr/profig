package profig

import scala.language.experimental.macros

/**
  * Platform-specific initialization for Scala.js
  */
object ProfigPlatform {
  /**
    * Called upon initialization of Profig at first use. Currently does nothing for Scala.js.
    */
  def init(instance: Profig): Unit = macro Macros.injection
}