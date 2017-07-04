package profig

import java.util.concurrent.atomic.AtomicBoolean

import io.circe._

import scala.collection.JavaConverters._
import scala.language.experimental.macros

object Config extends ConfigPath(Nil) {
  private val initialized = new AtomicBoolean(false)
  private[profig] var json: Json = Json.obj()

  def init(args: Seq[String],
           loadEnvironment: Boolean = true,
           loadProperties: Boolean = true): Unit = if (initialized.compareAndSet(false, true)) {
    val env = if (loadEnvironment) {
      ConfigUtil.map2Json(System.getenv().asScala.toMap)
    } else {
      Json.obj()
    }
    val props = if (loadProperties) {
      ConfigUtil.properties2Json(System.getProperties)
    } else {
      Json.obj()
    }
    val argsJson = ConfigUtil.args2Json(args)
    json = env.deepMerge(props).deepMerge(argsJson)
  }
}

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

  def merge(json: Json): Unit = combine(json, defaults = false)
  def defaults(json: Json): Unit = combine(json, defaults = true)

  protected def combine(json: Json, defaults: Boolean): Unit = synchronized {
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