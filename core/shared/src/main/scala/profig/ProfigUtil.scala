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
    val hashMap = new mutable.LinkedHashMap[String, ujson.Value]
    map.foreach {
      case (key, value) => hashMap += key -> string2JSON(value).value
    }
    Json(new Obj(hashMap))
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
      case NamedKeyValue(key, value) => named += key -> string2JSON(value).value
      case NamedFlag(key) => {
        flag.foreach { f =>
          named += f -> ujson.True
        }
        flag = Option(key)
      }
      case arg => flag match {
        case Some(key) => {
          named += key -> string2JSON(arg).value
          flag = None
        }
        case None => anonymous = string2JSON(arg).value :: anonymous
      }
    }
    anonymous = anonymous.reverse

    val argsNamed = anonymous.zipWithIndex.map {
      case (json, index) => s"arg${index + 1}" -> json
    }
    val argsList = List("args" -> ujson.Arr(anonymous: _*))
    val allArgsList = List("allArgs" -> ujson.Arr(args.map(string2JSON).map(_.value): _*))

    val map = new mutable.LinkedHashMap[String, ujson.Value]
    (argsNamed ::: argsList ::: allArgsList ::: named.toList).foreach {
      case (key, value) => map += key -> value
    }
    Json(new Obj(map))
  }

  /**
    * Converts a String based on its value into a Json object.
    */
  def string2JSON(s: String): Json = Json(read[ujson.Value](s))

  /**
    * Creates a Json representation breaking `name` for dot-separation.
    */
  def createJson(name: String, value: ujson.Value): Json = {
    val index = name.indexOf('.')
    if (index == -1) {
      val map = new mutable.LinkedHashMap[String, ujson.Value]
      map += name -> value
      Json(Obj(map))
    } else {
      val n = name.substring(0, index)
      val map = new mutable.LinkedHashMap[String, ujson.Value]
      map += n -> createJson(name.substring(index + 1), value).value
      Json(Obj(map))
    }
  }
}
