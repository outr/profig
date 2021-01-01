package profig

import io.circe._

import scala.annotation.tailrec
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
  def as[T](implicit decoder: Decoder[T]): T = JsonUtil.fromJson[T](apply())

  /**
    * Loads this path out as the defined type `T`. If no value is set for this path, the default will be used.
    *
    * @param default the default to be used if this path is empty
    * @tparam T the type to represent the current path
    * @return T
    */
  def as[T](default: => T)(implicit decoder: Decoder[T]): T = get().map(JsonUtil.fromJson[T]).getOrElse(default)

  /**
    * Convenience functionality similar to `as` but returns an option if set.
    *
    * @tparam T the type to represent the current path
    * @return T
    */
  def opt[T](implicit decoder: Decoder[T]): Option[T] = get().map(JsonUtil.fromJson[T])

  /**
    * Stores the supplied value into this path.
    *
    * @param value the value to store
    * @tparam T the type of value
    */
  def store[T](value: T)(implicit encoder: Encoder[T]): Unit = merge(JsonUtil.toJson[T](value))

  /**
    * Returns a Json representation of this path if there is anything defined at this level.
    *
    * @return Option[Json]
    */
  def get(): Option[Json] = {
    @tailrec
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
    * Merges a Json object to this path.
    */
  def merge(json: Json, `type`: MergeType = MergeType.Overwrite): Unit = synchronized {
    if (path.nonEmpty) {
      val updated = ProfigUtil.createJson(path.mkString("."), json)
      `type` match {
        case MergeType.Overwrite => instance.modify(_.deepMerge(updated))
        case MergeType.Add => instance.modify(updated.deepMerge)
      }
    } else {
      `type` match {
        case MergeType.Overwrite => instance.modify(_.deepMerge(json))
        case MergeType.Add => instance.modify(json.deepMerge)
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
      @tailrec
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