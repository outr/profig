package profig

import java.io.StringReader
import java.util.Properties

import io.circe.{ACursor, Json}

import scala.language.experimental.macros

/**
  * ConfigPath is the core of functionality in Profig. Config extends from it for the root path and is used for looking
  * up deeper paths as well.
  *
  * @param path the path defined within the configuration
  */
class ConfigPath(val path: List[String]) {
  /**
    * Look up a deeper path below the current path.
    *
    * @return ConfigPath
    */
  def apply(path1: String, path2: String*): ConfigPath = {
    val list = path2List(path1) ::: path2.flatMap(path2List).toList
    new ConfigPath(this.path ::: list)
  }

  /**
    * Loads this path out as the defined type `T`.
    *
    * @tparam T the type to represent the current path
    * @return T
    */
  def as[T]: T = macro Macros.as[T]

  /**
    * Stores the supplied value into this path.
    *
    * @param value the value to store
    * @tparam T the type of value
    */
  def store[T](value: T): Unit = macro Macros.store[T]

  /**
    * Returns a Json representation of this path if there is anything defined at this level.
    *
    * @return Option[Json]
    */
  def get(): Option[Json] = {
    def find(path: List[String], cursor: ACursor): Option[Json] = if (path.tail.isEmpty) {
      cursor.get[Json](path.head).toOption
    } else {
      find(path.tail, cursor.downField(path.head))
    }
    if (path.nonEmpty) {
      find(path.tail, Config.json.hcursor.downField(path.head))
    } else {
      Some(Config.json)
    }
  }

  /**
    * Returns a Json representation of this path. Works similar to `get()`, except will return an empty Json object if
    * there is nothing at this level.
    *
    * @return Json
    */
  def apply(): Json = get().getOrElse(Json.obj())

  /**
    * True if this path exists in the Config
    */
  def exists(): Boolean = get().nonEmpty

  /**
    * Merges a sequence of args. This is primarily useful for merging command-line arguments.
    *
    * @see profig.ConfigApplication for a managed mechanism for this
    */
  def merge(args: Seq[String]): Unit = combine(args, defaults = false)

  /**
    * Loads defaults for a sequence of args. This is primarily useful for loading command-line arguments.
    */
  def defaults(args: Seq[String]): Unit = combine(args, defaults = true)

  /**
    * Merges a string of content auto-detected to JSON or Properties.
    */
  def merge(string: String): Unit = combine(string, defaults = false)

  /**
    * Loads defaults for a string of content auto-detected to JSON or Properties.
    */
  def defaults(string: String): Unit = combine(string, defaults = true)

  /**
    * Merges a Json object to this path.
    */
  def merge(json: Json): Unit = combine(json, defaults = false)

  /**
    * Loads defaults from this Json object at this path.
    */
  def defaults(json: Json): Unit = combine(json, defaults = true)

  /**
    * Merges a Properties object to this path.
    */
  def merge(properties: Properties): Unit = combine(properties, defaults = false)

  /**
    * Loads defaults from this Properties object at this path.
    */
  def defaults(properties: Properties): Unit = combine(properties, defaults = true)

  /**
    * Combines a string of content auto-detected to JSON or Properties.
    */
  def combine(string: String, defaults: Boolean): Unit = {
    if (string.trim.startsWith("{")) {      // JSON detected
      val json = io.circe.parser.parse(string) match {
        case Left(failure) => throw new RuntimeException(s"Unable to parse $string to JSON.", failure)
        case Right(value) => value
      }
      combine(json, defaults)
    } else {                                // Properties?
      val properties = new Properties()
      properties.load(new StringReader(string))
      combine(properties, defaults)
    }
  }

  /**
    * Combines a sequence of args at this path.
    */
  def combine(args: Seq[String], defaults: Boolean): Unit = {
    val json = ConfigUtil.args2Json(args)
    combine(json, defaults)
  }

  /**
    * Combines a properties object at this path.
    */
  def combine(properties: Properties, defaults: Boolean): Unit = {
    combine(ConfigUtil.properties2Json(properties), defaults)
  }

  /**
    * Combines a Json instance at this path.
    */
  def combine(json: Json, defaults: Boolean): Unit = synchronized {
    if (path.nonEmpty) {
      val updated = ConfigUtil.createJson(path.mkString("."), json)
      if (defaults) {
        Config.json = updated.deepMerge(Config.json)
      } else {
        Config.json = Config.json.deepMerge(updated)
      }
    } else {
      if (defaults) {
        Config.json = json.deepMerge(Config.json)
      } else {
        Config.json = Config.json.deepMerge(json)
      }
    }
  }

  private def path2List(path: String): List[String] = path.split('.').toList
}
