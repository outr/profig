package profig

import scala.language.experimental.macros

/**
  * Platform-specific initialization for Scala.js
  */
object ProfigPlatform {
  def loadFiles(instance: Profig, entries: List[ConfigurationPath]): Unit = macro Macros.injection
}