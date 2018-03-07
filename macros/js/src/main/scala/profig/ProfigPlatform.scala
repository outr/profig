package profig

import scala.language.experimental.macros

/**
  * Platform-specific initialization for Scala.js
  */
object ProfigPlatform {
  def isJS: Boolean = true
  def isJVM: Boolean = false
}