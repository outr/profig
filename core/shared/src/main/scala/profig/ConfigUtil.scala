package profig

import java.util.Properties

import io.circe._

import scala.annotation.tailrec
import scala.collection.JavaConverters._

/**
  * Primarily intended for internal use.
  */
object ConfigUtil {
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

  /**
    * Converts a sequence of args into a Json object with dot-separation.
    */
  @tailrec
  final def args2Json(args: Seq[String], json: Json = Json.obj(), index: Int = 1): Json = if (args.isEmpty) {
    json
  } else {
    var seq = args
    val first = seq.head
    seq = seq.tail
    var i = index

    val j: Json = if (first.startsWith("-")) {
      val n = if (first.startsWith("--")) {
        first.substring(2)
      } else {
        first.substring(1)
      }
      val equalsIndex = n.indexOf('=')
      if (equalsIndex > 0) {
        createJson(n.substring(0, equalsIndex), string2JSON(n.substring(equalsIndex + 1)))
      } else if (seq.isEmpty) {
        createJson(n, Json.True)
      } else {
        val second = seq.head
        seq = seq.tail
        createJson(n, string2JSON(second))
      }
    } else {
      i += 1
      Json.obj(s"arg$index" -> string2JSON(first))
    }
    args2Json(seq, json.deepMerge(j), i)
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