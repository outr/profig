package profig

import java.util.concurrent.atomic.AtomicBoolean

import io.circe._

import scala.collection.JavaConverters._

object Config {
  private val initialized = new AtomicBoolean(false)
  private var json: Json = Json.obj()

  def init(args: Seq[String],
           loadEnvironment: Boolean = true,
           loadProperties: Boolean = true): Unit = if (initialized.compareAndSet(false, true)) {
    val env = if (loadEnvironment) {
      Json.obj("environment" -> ConfigUtil.map2Json(System.getenv().asScala.toMap))
    } else {
      Json.obj()
    }
    val props = if (loadProperties) {
      Json.obj("properties" -> ConfigUtil.properties2Json(System.getProperties))
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

  def as[T](path: String)(implicit decoder: Decoder[T]): T = decoder.decodeJson(apply(path)) match {
    case Left(failure) => throw failure
    case Right(t) => t
  }

  def store[T](t: T, path: String = "")(implicit encoder: Encoder[T]): Unit = {
    val json = encoder(t)
    merge(json, path2List(path): _*)
  }

  def merge(json: Json, path: String*): Unit = synchronized {
    if (path.nonEmpty) {
      val updated = ConfigUtil.createJson(path.mkString("."), json)
      this.json = this.json.deepMerge(updated)
    } else {
      this.json = this.json.deepMerge(json)
    }
  }

  private def path2List(path: String): List[String] = path.split('.').toList

  def main(args: Array[String]): Unit = {
    import io.circe.generic.auto._
    init(args)
    store(Person("Matt", 38), "people.me")
    println(as[Person]("people.me"))
  }
}

case class Person(name: String, age: Int = 21)