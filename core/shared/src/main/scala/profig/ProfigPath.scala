package profig

import hierarchical._
import hierarchical.rw._

import scala.language.experimental.macros

/**
  * ProfigPath is the core of functionality in Profig. Profig extends from it for the root path and is used for looking
  * up deeper paths as well.
  */
trait ProfigPath extends ProfigPathPlatform {
  def instance: Profig
  def path: Path

  /**
    * Look up a deeper path below the current path.
    *
    * @return ProfigPath
    */
  def apply(path: String*): ProfigPath = ProfigPath(instance, this.path \\ new Path(path.toList))

  /**
    * Loads this path out as the defined type `T`.
    *
    * @tparam T the type to represent the current path
    * @return T
    */
  def as[T: Writer]: T = apply().as[T]

  /**
    * Loads this path out as the defined type `T`. If no value is set for this path, the default will be used.
    *
    * @param default the default to be used if this path is empty
    * @tparam T the type to represent the current path
    * @return T
    */
  def as[T: Writer](default: => T): T = opt[T].getOrElse(default)

  /**
    * Convenience functionality similar to `as` but returns an option if set.
    *
    * @tparam T the type to represent the current path
    * @return T
    */
  def opt[T: Writer]: Option[T] = get().map(_.as[T])

  /**
    * Stores the supplied value into this path.
    *
    * @param value the value to store
    * @tparam T the type of value
    */
  def store[T: Reader](value: T): Unit = merge(value.toValue)

  /**
    * Returns a Json representation of this path if there is anything defined at this level.
    *
    * @return Option[Json]
    */
  def get(): Option[Value] = instance.json.get(path)

  /**
    * Returns a Json representation of this path. Works similar to `get()`, except will return an empty Json object if
    * there is nothing at this level.
    *
    * @return Json
    */
  def apply(): Value = instance.json(path)

  /**
    * True if this path exists in the config
    */
  def exists(): Boolean = get().nonEmpty

  /**
    * Merges a Json object to this path.
    */
  def merge(json: Value, `type`: MergeType = MergeType.Overwrite): Unit = instance.modify(_.merge(json, path, `type`))

  /**
    * Removes a field from this path.
    *
    * @param field the field below this path to remove
    */
  def remove(field: String): Unit = instance.modify(_.remove(path \ field))

  def remove(): Unit = instance.modify(_.remove(path))
}

object ProfigPath {
  def apply(instance: Profig, path: Path): ProfigPath = new ProfigSubPath(instance, path)

  class ProfigSubPath(override val instance: Profig, override val path: Path) extends ProfigPath
}