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
    loadEnvironmentMap(envMap, `type`)
  }

  def loadEnvironmentMap(map: Map[String, String], `type`: MergeType = MergeType.Overwrite): Unit = {
    val envConverted = ProfigUtil.map2Json(map.map {
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
  def empty: Profig = new Profig

  loadDefaults()

  /**
    * Loads the default configuration sources in ascending priority, each layer overriding the previous on conflict:
    * config files (classpath, then filesystem), environment variables, system properties, and command-line arguments.
    *
    * This is invoked automatically the first time the Profig object is used and normally never needs to be called
    * directly. Loading happens against a detached instance and is swapped in atomically, so concurrent readers never
    * observe a partially loaded state.
    */
  def loadDefaults(): Unit = synchronized {
    // Failures must not escape: an exception thrown from the object initializer would permanently poison this object
    val defaults = empty
    attempt("configuration files")(initProfig(defaults))
    attempt("environment variables")(defaults.loadEnvironmentVariables())
    attempt("system properties")(defaults.loadProperties(MergeType.Overwrite))
    attempt("command-line arguments")(defaultArguments match {
      case Nil => // No arguments detected
      case arguments => defaults.merge(ProfigUtil.args2Json(arguments))
    })
    modify(_ => defaults.json)
  }

  private def attempt(description: String)(f: => Unit): Unit = try {
    f
  } catch {
    case t: Throwable => System.err.println(s"Profig failed to load $description: ${t.getMessage}")
  }

  /**
    * Discards all stored values and restores the freshly loaded default configuration.
    */
  def reset(): Unit = loadDefaults()
}
