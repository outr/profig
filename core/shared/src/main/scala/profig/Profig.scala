package profig

import scala.jdk.CollectionConverters._
import scala.language.experimental.macros

class Profig extends ProfigPath {
  private var _json: Json = Json()
  private var _lastModified: Long = System.currentTimeMillis()

  def json: Json = _json

  protected[profig] def modify(f: Json => Json): Unit = synchronized {
    _json = f(_json)
    _lastModified = System.currentTimeMillis()
  }

  def lastModified: Long = _lastModified

  override def instance: Profig = this
  override def path: List[String] = Nil

  def loadEnvironmentVariables(`type`: MergeType = MergeType.Add): Unit = {
    val envMap = System.getenv().asScala.toMap
    val envConverted = ProfigUtil.map2Json(envMap.map {
      case (key, value) => key.toLowerCase.replace('_', '.') -> value
    })
    merge(envConverted, `type`)
  }

  def loadProperties(`type`: MergeType = MergeType.Add): Unit = {
    val props = ProfigUtil.properties2Json(System.getProperties)
    merge(props, `type`)
  }

  override def remove(): Unit = modify(_ => Json())

  def clear(): Unit = remove()
}

/**
  * Profig provides access to environment variables, properties, and other configuration all merged together into one
  * powerful system. Uses JSON internally to provide merging and integration. Paths are dot-separated.
  */
object Profig extends Profig {
  private var loaded = false

  def isLoaded: Boolean = loaded

  def empty: Profig = new Profig

  /**
    * Initializes Profig
    *
    * @param loadProperties whether to load system properties
    * @param loadEnvironmentVariables whether to load environment variables
    * @param loadModules whether to load external modules (ex. XML, Hocon, YAML support)
    * @param ec the execution context to run this in
    */
  def init(loadProperties: Boolean = true,
           loadEnvironmentVariables: Boolean = true,
           loadModules: Boolean = true): Unit = synchronized {
    if (!loaded) {
      loaded = true
      if (loadProperties) {
        this.loadProperties()
      }
      if (loadEnvironmentVariables) {
        this.loadEnvironmentVariables()
      }
      initProfig(loadModules)
    }
  }
}
