package profig

import java.util.concurrent.atomic.AtomicBoolean

import scala.language.experimental.macros

/**
  * Platform-specific initialization for Scala.js
  */
object ProfigPlatform {
  val initialized: AtomicBoolean = new AtomicBoolean(false)

  /**
    * Called upon initialization of Profig at first use. Currently does nothing for Scala.js.
    */
  def init(instance: Profig): Unit = macro PlatformMacros.init
}