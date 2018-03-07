package profig

/**
  * Platform-specific initialization for JVM
  */
object ProfigPlatform {
  def isJS: Boolean = false
  def isJVM: Boolean = true
}