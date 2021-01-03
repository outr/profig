package profig

import ujson.Obj

import java.util.Properties
import upickle.default._

import scala.collection.mutable
import scala.jdk.CollectionConverters._

/**
  * Primarily intended for internal use.
  */
object ProfigUtil {
  /**
    * Converts a `Map[String, String]` into a Json object with dot-separation.
    */
  def map2Json(map: Map[String, String]): Json = {
    val json = Json()
    map.foreach {
      case (key, value) => {
        val path = key.split('.').toList
        json.set(Json.string(value).value, path: _*)
      }
    }
    json
  }

  /**
    * Converts a `Properties` into a Json object with dot-separation.
    */
  def properties2Json(properties: Properties): Json = {
    val map = properties.asScala.map {
      case (key, value) => key -> value
    }.toMap
    map2Json(map)
  }

  private val NamedKeyValue = """-{1,}(.+)=(.+)""".r
  private val NamedFlag = """-{1,}(.+)""".r

  /**
    * Converts a sequence of args into a Json object with dot-separation.
    */
  def args2Json(args: Seq[String]): Json = {
    var anonymous = List.empty[ujson.Value]
    var named = Map.empty[String, ujson.Value]
    var flag = Option.empty[String]
    args.foreach {
      case NamedKeyValue(key, value) => named += key -> Json.string(value).value
      case NamedFlag(key) => {
        flag.foreach { f =>
          named += f -> ujson.True
        }
        flag = Option(key)
      }
      case arg => flag match {
        case Some(key) => {
          named += key -> Json.string(arg).value
          flag = None
        }
        case None => anonymous = Json.string(arg).value :: anonymous
      }
    }
    anonymous = anonymous.reverse

    val argsNamed = anonymous.zipWithIndex.map {
      case (json, index) => s"arg${index + 1}" -> json
    }
    val argsList = List("args" -> ujson.Arr(anonymous: _*))
    val allArgsList = List("allArgs" -> ujson.Arr(args.map(Json.string).map(_.value): _*))

    val json = Json()
    (argsNamed ::: argsList ::: allArgsList ::: named.toList).foreach {
      case (key, value) => {
        val path = key.split('.').toList
        json.set(value, path: _*)
      }
    }
    json
  }
}
