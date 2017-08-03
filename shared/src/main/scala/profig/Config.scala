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
  private[profig] val envConverted = ConfigUtil.map2Json(envMap.map {
    case (key, value) => key.toLowerCase.replace('_', '.') -> value
  })
  private[profig] val props = ConfigUtil.properties2Json(System.getProperties)
  private[profig] var json: Json = envConverted.deepMerge(props)

  /**
    * Initializes Profig. This must be invoked prior to using. Multiple invocations are allowed, but initialization only
    * occurs the first time. Additional calls will still merge args submitted by this method.
    *
    * @param args the command-line arguments to merge into the configuration, if any
    */
  def init(args: Seq[String] = Seq.empty): Unit = macro Macros.init

  /**
    * Specialized version of init when being used to load configuration for use with a Macro at compile-time. This is a
    * work-around for parser limitations in Scala.js when running on the JVM.
    *
    * Warning: this should only be used when being invoked from another Macro like the following:
    *   `context.eval(reify(profig.Config.initMacro(Nil)))`
    *
    * @param args the command-line arguments to merge into the configuration, if any
    */
  def initMacro(args: Seq[String]): Unit = macro Macros.initMacro
}