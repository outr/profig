package profig

import fabric._

import scala.jdk.CollectionConverters._
import scala.language.experimental.macros

class Profig extends ProfigPath {
  private var _json: Json = obj()
  private var _lastModified: Long = System.currentTimeMillis()

  def json: Json = _json

  protected[profig] def modify(f: Json => Json): Unit = synchronized {
    _json = f(_json)
    _lastModified = System.currentTimeMillis()
  }

  def lastModified: Long = _lastModified

  override def instance: Profig = this
  override def path: JsonPath = JsonPath.empty

  def loadEnvironmentVariables(`type`: MergeType = MergeType.Overwrite): Unit = {
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

  override def remove(): Unit = modify(_ => obj())

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
    */
  def init(loadProperties: Boolean = true,
           loadEnvironmentVariables: Boolean = true): Unit = synchronized {
    if (!loaded) {
      loaded = true
      if (loadProperties) {
        this.loadProperties()
      }
      if (loadEnvironmentVariables) {
        this.loadEnvironmentVariables()
      }
    }
  }

  def reset(): Unit = synchronized {
    clear()
    loaded = false
  }
}
