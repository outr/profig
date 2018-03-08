package profig

import io.circe.Json

import scala.collection.JavaConverters._
import scala.language.experimental.macros

class Profig(val parent: Option[Profig] = Some(Profig)) extends ProfigPath {
  private var _local: Json = Json.obj()
  private var _global: Json = _local

  private var _lastModified: Long = System.currentTimeMillis()

  updateGlobal()

  def json: Json = {
    if (parent.map(_.lastModified).getOrElse(0L) > lastModified) {
      updateGlobal()
    }
    _global
  }

  protected[profig] def modify(f: Json => Json): Unit = synchronized {
    _local = f(_local)
    updateGlobal()
    _lastModified = System.currentTimeMillis()
  }

  def lastModified: Long = _lastModified

  override def instance: Profig = this
  override def path: List[String] = Nil

  def loadDefaults(): Unit = macro Macros.loadDefaults

  def load(entries: List[ConfigurationPath]): Unit = macro Macros.load

  def loadEnvironmentVariables(asDefault: Boolean = true): Unit = {
    val envMap = System.getenv().asScala.toMap
    val envConverted = ConfigUtil.map2Json(envMap.map {
      case (key, value) => key.toLowerCase.replace('_', '.') -> value
    })
    if (asDefault) {
      defaults(envConverted)
    } else {
      merge(envConverted)
    }
  }

  def loadProperties(asDefault: Boolean = true): Unit = {
    val props = ConfigUtil.properties2Json(System.getProperties)
    if (asDefault) {
      defaults(props)
    } else {
      merge(props)
    }
  }

  def child(): Profig = new Profig(Some(this))

  private def updateGlobal(): Unit = synchronized {
    parent match {
      case Some(p) => _global = p.json.deepMerge(_local)
      case None => _global = _local
    }
  }

  override def remove(): Unit = modify(_ => Json.obj())

  def clear(): Unit = remove()
}

/**
  * Profig provides access to environment variables, properties, and other configuration all merged together into one
  * powerful system. Uses JSON internally to provide merging and integration. Paths are dot-separated.
  */
object Profig extends Profig(None) {
  loadProperties()
  loadEnvironmentVariables()

  def apply(parent: Option[Profig]): Profig = new Profig(parent)
}
