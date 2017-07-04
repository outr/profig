package profig

import java.io.StringReader
import java.util.Properties

import io.circe.{ACursor, Json}

import scala.language.experimental.macros

class ConfigPath(val path: List[String]) {
  def apply(path: String*): ConfigPath = {
    val list = path.flatMap(path2List).toList
    new ConfigPath(this.path ::: list)
  }

  def as[T]: T = macro Macros.as[T]

  def store[T](value: T): Unit = macro Macros.store[T]

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

  def apply(): Json = get().getOrElse(Json.obj())

  def exists(): Boolean = get().nonEmpty

  def merge(args: Seq[String]): Unit = combine(args, defaults = false)
  def defaults(args: Seq[String]): Unit = combine(args, defaults = true)

  def merge(string: String): Unit = combine(string, defaults = false)
  def defaults(string: String): Unit = combine(string, defaults = true)

  def merge(json: Json): Unit = combine(json, defaults = false)
  def defaults(json: Json): Unit = combine(json, defaults = true)

  def merge(properties: Properties): Unit = combine(properties, defaults = false)
  def defaults(properties: Properties): Unit = combine(properties, defaults = true)

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

  def combine(args: Seq[String], defaults: Boolean): Unit = {
    val json = ConfigUtil.args2Json(args)
    combine(json, defaults)
  }

  def combine(properties: Properties, defaults: Boolean): Unit = {
    combine(ConfigUtil.properties2Json(properties), defaults)
  }

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
