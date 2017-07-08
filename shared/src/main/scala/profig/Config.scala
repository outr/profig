package profig

import io.circe.Json

import scala.collection.JavaConverters._
import scala.language.experimental.macros

/**
  * Config provides access to environment variables, properties, and other configuration all merged together into one
  * powerful system. Uses JSON internally to provide merging and integration. Paths are dot-separated.
  *
  * @see profig.ConfigApplication for convenience initialization
  */
object Config extends ConfigPath(Nil) {
  private val envMap = System.getenv().asScala.toMap
  private[profig] val env = ConfigUtil.map2Json(envMap)
  private[profig] val envConverted = ConfigUtil.map2Json(envMap.map {
    case (key, value) => key.toLowerCase.replace('_', '.') -> value
  })
  private[profig] val props = ConfigUtil.properties2Json(System.getProperties)
  private[profig] var json: Json = env.deepMerge(envConverted).deepMerge(props)

  // Platform-specific initialization
  ProfigPlatform.init()
}