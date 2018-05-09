package profig

import java.util.Properties

import io.circe._

import scala.language.experimental.macros

/**
  * ProfigPath is the core of functionality in Profig. Profig extends from it for the root path and is used for looking
  * up deeper paths as well.
  */
trait ProfigPath {
  def instance: Profig
  def path: List[String]

  /**
    * Look up a deeper path below the current path.
    *
    * @return ProfigPath
    */
  def apply(path: String*): ProfigPath = {
    val list = path.toList.flatMap(path2List)
    ProfigPath(instance, this.path ::: list)
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
      cursor.get[Json](path.head) match {
        case Left(_) => None
        case Right(value) => Some(value)
      }
    } else {
      find(path.tail, cursor.downField(path.head))
    }
    if (path.nonEmpty) {
      if (path.tail.isEmpty) {
        instance.json.hcursor.get[Json](path.head) match {
          case Left(_) => None
          case Right(value) => Some(value)
        }
      } else {
        find(path.tail, instance.json.hcursor.downField(path.head))
      }
    } else {
      Some(instance.json)
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
    * True if this path exists in the config
    */
  def exists(): Boolean = get().nonEmpty

  /**
    * Merges a sequence of args. This is primarily useful for merging command-line arguments.
    */
  def merge(args: Seq[String]): Unit = combine(args, defaults = false)

  /**
    * Loads defaults for a sequence of args. This is primarily useful for loading command-line arguments.
    */
  def defaults(args: Seq[String]): Unit = combine(args, defaults = true)

  /**
    * Merges a string of content from the specified type.
    */
  def merge(string: String, `type`: FileType): Unit = combine(string, `type`, defaults = false)

  /**
    * Loads defaults for a string of the specified type.
    */
  def defaults(string: String, `type`: FileType): Unit = combine(string, `type`, defaults = true)

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
    * Combines a string of content auto-detected to JSON.
    */
  def combine(string: String, `type`: FileType, defaults: Boolean): Unit = {
    val json = ProfigLookupPath.toJson(string, `type`)
    combine(json, defaults)
  }

  /**
    * Combines a sequence of args at this path.
    */
  def combine(args: Seq[String], defaults: Boolean): Unit = {
    val json = ProfigUtil.args2Json(args)
    combine(json, defaults)
  }

  /**
    * Combines a properties object at this path.
    */
  def combine(properties: Properties, defaults: Boolean): Unit = {
    combine(ProfigUtil.properties2Json(properties), defaults)
  }

  /**
    * Combines a Json instance at this path.
    */
  def combine(json: Json, defaults: Boolean): Unit = synchronized {
    if (path.nonEmpty) {
      val updated = ProfigUtil.createJson(path.mkString("."), json)
      if (defaults) {
        instance.modify(updated.deepMerge)
      } else {
        instance.modify(_.deepMerge(updated))
      }
    } else {
      if (defaults) {
        instance.modify(json.deepMerge)
      } else {
        instance.modify(_.deepMerge(json))
      }
    }
  }

  /**
    * Removes a field from this path.
    *
    * @param field the field below this path to remove
    */
  def remove(field: String): Unit = synchronized {
    if (path.nonEmpty) {
      def recurse(path: List[String], cursor: ACursor): Json = if (path.isEmpty) {
        cursor.downField(field).delete.top.get
      } else {
        recurse(path.tail, cursor.downField(path.head))
      }

      instance.modify { json =>
        recurse(path.tail, json.hcursor.downField(path.head))
      }
    } else {
      instance.modify(json => Json.fromJsonObject(json.asObject.get.remove(field)))
    }
  }

  def remove(): Unit = instance(path.take(path.length - 1): _*).remove(path.last)

  private def path2List(path: String): List[String] = path.split('.').toList
}

object ProfigPath {
  def apply(instance: Profig, path: List[String]): ProfigPath = new ProfigSubPath(instance, path)

  class ProfigSubPath(override val instance: Profig, override val path: List[String]) extends ProfigPath
}