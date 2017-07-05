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
  private[profig] val env = ConfigUtil.map2Json(System.getenv().asScala.toMap)
  private[profig] val props = ConfigUtil.properties2Json(System.getProperties)
  private[profig] var json: Json = env.deepMerge(props)

  // Platform-specific initialization
  ProfigPlatform.init()
}