package profig

import java.util.Properties
import java.util.concurrent.atomic.AtomicBoolean

import io.circe._

import scala.collection.JavaConverters._
import scala.language.experimental.macros

object Config {
  private val initialized = new AtomicBoolean(false)
  private var json: Json = Json.obj()

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

  def get(path: String): Option[Json] = {
    val list = path2List(path)
    def find(path: List[String], cursor: ACursor): Option[Json] = if (path.tail.isEmpty) {
      cursor.get[Json](path.head).toOption
    } else {
      find(path.tail, cursor.downField(path.head))
    }
    if (list.nonEmpty) {
      find(list.tail, json.hcursor.downField(list.head))
    } else {
      Some(json)
    }
  }

  def apply(path: String): Json = get(path).getOrElse(Json.obj())

  def as[T](path: String): T = macro Macros.as[T]

  def store[T](value: T, path: String = ""): Unit = macro Macros.store[T]

  def merge(json: Json, path: String = "", defaults: Boolean = false): Unit = synchronized {
    val list = path2List(path)
    if (path.nonEmpty) {
      val updated = ConfigUtil.createJson(list.mkString("."), json)
      if (defaults) {
        this.json = updated.deepMerge(this.json)
      } else {
        this.json = this.json.deepMerge(updated)
      }
    } else {
      if (defaults) {
        this.json = json.deepMerge(this.json)
      } else {
        this.json = this.json.deepMerge(json)
      }
    }
  }

  def merge(properties: Properties, path: String = "", defaults: Boolean = false): Unit = {
    val json = ConfigUtil.properties2Json(properties)
    merge(json, path, defaults)
  }

  private def path2List(path: String): List[String] = path.split('.').toList
}