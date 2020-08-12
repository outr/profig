package profig

import io.circe.Json

import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters._
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
  private var loaded = false

  def isLoaded: Boolean = loaded

  def apply(parent: Option[Profig]): Profig = new Profig(parent)

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
           loadModules: Boolean = true)
          (implicit ec: ExecutionContext): Future[Unit] = synchronized {
    if (loaded) {
      Future.successful(())
    } else {
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
