package profig

import scala.language.experimental.macros

/**
  * ProfigPath is the core of functionality in Profig. Profig extends from it for the root path and is used for looking
  * up deeper paths as well.
  */
trait ProfigPath extends ProfigPathPlatform {
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
  def as[T: Reader]: T = JsonUtil.fromJson[T](apply())

  /**
    * Loads this path out as the defined type `T`. If no value is set for this path, the default will be used.
    *
    * @param default the default to be used if this path is empty
    * @tparam T the type to represent the current path
    * @return T
    */
  def as[T: Reader](default: => T): T = get().map(JsonUtil.fromJson[T]).getOrElse(default)

  /**
    * Convenience functionality similar to `as` but returns an option if set.
    *
    * @tparam T the type to represent the current path
    * @return T
    */
  def opt[T: Reader]: Option[T] = {
    get() match {
      case Some(json) => json.value match {
        case arr: ujson.Arr => if (arr.value.isEmpty) {
          None
        } else {
          Some(JsonUtil.fromJson[T](new Json(arr.value.head)))
        }
        case _ => Some(JsonUtil.fromJson[T](json))
      }
      case None => None
    }
  }

  /**
    * Stores the supplied value into this path.
    *
    * @param value the value to store
    * @tparam T the type of value
    */
  def store[T: Writer](value: T): Unit = merge(JsonUtil.toJson[T](value))

  /**
    * Returns a Json representation of this path if there is anything defined at this level.
    *
    * @return Option[Json]
    */
  def get(): Option[Json] = instance.json.get(path: _*)

  /**
    * Returns a Json representation of this path. Works similar to `get()`, except will return an empty Json object if
    * there is nothing at this level.
    *
    * @return Json
    */
  def apply(): Json = instance.json.obj(path: _*)

  /**
    * True if this path exists in the config
    */
  def exists(): Boolean = get().nonEmpty

  /**
    * Merges a Json object to this path.
    */
  def merge(json: Json, `type`: MergeType = MergeType.Overwrite): Unit = synchronized {
    `type` match {
      case MergeType.Overwrite => instance.json.merge(json.value, path: _*)
      case MergeType.Add => instance.json.defaults(json.value, path: _*)
    }
  }

  /**
    * Removes a field from this path.
    *
    * @param field the field below this path to remove
    */
  def remove(field: String): Unit = synchronized {
    instance.json.remove((path ::: List(field)): _*)
  }

  def remove(): Unit = instance(path.take(path.length - 1): _*).remove(path.last)

  private def path2List(path: String): List[String] = path.split('.').toList
}

object ProfigPath {
  def apply(instance: Profig, path: List[String]): ProfigPath = new ProfigSubPath(instance, path)

  class ProfigSubPath(override val instance: Profig, override val path: List[String]) extends ProfigPath
}