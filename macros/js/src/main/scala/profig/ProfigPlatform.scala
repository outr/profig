package profig

import io.circe.Json

import scala.language.experimental.macros

/**
  * Platform-specific initialization for Scala.js
  */
object ProfigPlatform {
  val yamlConversion: Option[String => Json] = None
  def isJS: Boolean = true
  def isJVM: Boolean = false
}