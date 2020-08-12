package profig

import java.util.Properties

import io.circe._

import scala.jdk.CollectionConverters._

/**
  * Primarily intended for internal use.
  */
object ProfigUtil {
  /**
    * Converts a `Map[String, String]` into a Json object with dot-separation.
    */
  def map2Json(map: Map[String, String]): Json = {
    var json = Json.obj()
    map.foreach {
      case (key, value) => json = json.deepMerge(createJson(key, string2JSON(value)))
    }
    json
  }

  /**
    * Converts a `Properties` into a Json object with dot-separation.
    */
  def properties2Json(properties: Properties): Json = {
    val map = properties.asScala.map {
      case (key, value) => key.toString -> value.toString
    }.toMap
    map2Json(map)
  }

  private val NamedKeyValue = """-{1,}(.+)=(.+)""".r
  private val NamedFlag = """-{1,}(.+)""".r

  /**
    * Converts a sequence of args into a Json object with dot-separation.
    */
  def args2Json(args: Seq[String]): Json = {
    var anonymous = List.empty[Json]
    var named = Map.empty[String, Json]
    var flag = Option.empty[String]
    args.foreach {
      case NamedKeyValue(key, value) => named += key -> string2JSON(value)
      case NamedFlag(key) => {
        flag.foreach { f =>
          named += f -> Json.True
        }
        flag = Option(key)
      }
      case arg => flag match {
        case Some(key) => {
          named += key -> string2JSON(arg)
          flag = None
        }
        case None => anonymous = string2JSON(arg) :: anonymous
      }
    }
    anonymous = anonymous.reverse

    val argsNamed = anonymous.zipWithIndex.map {
      case (json, index) => s"arg${index + 1}" -> json
    }
    val argsList = List("args" -> Json.arr(anonymous: _*))
    val allArgsList = List("allArgs" -> Json.arr(args.map(string2JSON): _*))
    var obj = Json.obj(argsNamed ::: argsList ::: allArgsList: _*)
    named.toList.map {
      case (key, value) => createJson(key, value)
    }.foreach { json =>
      obj = json.deepMerge(obj)
    }
    obj
  }

  /**
    * Converts a String based on its value into a Json object.
    */
  def string2JSON(s: String): Json = Json.fromString(s)

  /**
    * Creates a Json representation breaking `name` for dot-separation.
    */
  def createJson(name: String, value: Json): Json = {
    val index = name.indexOf('.')
    if (index == -1) {
      Json.obj(name -> value)
    } else {
      val n = name.substring(0, index)
      Json.obj(n -> createJson(name.substring(index + 1), value))
    }
  }
}