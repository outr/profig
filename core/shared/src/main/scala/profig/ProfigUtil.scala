package profig

import java.util.Properties
import hierarchical._

import scala.jdk.CollectionConverters._

/**
  * Primarily intended for internal use.
  */
object ProfigUtil {
  /**
    * Converts a `Map[String, String]` into a Json object with dot-separation.
    */
  def map2Json(map: Map[String, String]): Value = Obj.process(map)

  /**
    * Converts a `Properties` into a Json object with dot-separation.
    */
  def properties2Json(properties: Properties): Value = {
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
  def args2Json(args: Seq[String]): Value = {
    var anonymous = List.empty[Value]
    var named = Map.empty[String, Value]
    var flag = Option.empty[String]
    args.foreach {
      case NamedKeyValue(key, value) => named += key -> str(value)
      case NamedFlag(key) => {
        flag.foreach { f =>
          named += f -> true
        }
        flag = Option(key)
      }
      case arg => flag match {
        case Some(key) => {
          named += key -> str(arg)
          flag = None
        }
        case None => anonymous = str(arg) :: anonymous
      }
    }
    anonymous = anonymous.reverse

    val argsNamed = anonymous.zipWithIndex.map {
      case (json, index) => s"arg${index + 1}" -> json
    }
    val argsList = List("args" -> Arr(anonymous.toVector))
    val allArgsList = List("allArgs" -> Arr(args.map(str).toVector))

    var v: Value = obj()
    (argsNamed ::: argsList ::: allArgsList ::: named.toList).foreach {
      case (key, value) => v = v.merge(value, Path.parse(key))
    }
    v
  }
}