package profig

import io.circe.Json

/**
  * Platform-specific initialization for JVM
  */
object ProfigPlatform {
  val yamlConversion: Option[String => Json] = Some(ProfigLookupPath.yamlString2Json)
  def isJS: Boolean = false
  def isJVM: Boolean = true
}