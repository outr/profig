package profig

import Pickler._

object JsonUtil {
  def fromJson[T: Reader](json: Json): T = try {
    json.as[T]
  } catch {
    case t: Throwable => throw new RuntimeException(s"Failed to convert: $json", t)
  }
  def toJson[T: Writer](value: T): Json = new Json(writeJs(value))

  def fromJsonString[T: Reader](jsonString: String): T = fromJson[T](Json.parse(jsonString))
  def toJsonString[T: Writer](value: T): String = toJson[T](value).toString
}