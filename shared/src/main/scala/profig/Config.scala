package profig

import java.util.concurrent.atomic.AtomicBoolean

import io.circe.Json

import scala.collection.JavaConverters._
import scala.language.experimental.macros

object Config extends ConfigPath(Nil) {
  private[profig] val env = ConfigUtil.map2Json(System.getenv().asScala.toMap)
  private[profig] val props = ConfigUtil.properties2Json(System.getProperties)
  private[profig] var json: Json = env.deepMerge(props)

  // Platform-specific initialization
  ProfigPlatform.init()
}