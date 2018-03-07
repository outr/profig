package profig

import io.circe.Json

/**
  * Platform-specific initialization for JVM
  */
object ProfigPlatform {
  val yamlConversion: Option[String => Json] = Some(ConfigurationPath.yamlString2Json)
  def isJS: Boolean = false
  def isJVM: Boolean = true
}