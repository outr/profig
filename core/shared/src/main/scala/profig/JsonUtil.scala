package profig

import upickle.default._

object JsonUtil {
  def fromJson[T: Reader](json: Json): T = json.as[T]
  def toJson[T: Writer](value: T): Json = Json(value)

  def fromJsonString[T: Reader](jsonString: String): T = fromJson[T](Json(jsonString))
  def toJsonString[T: Writer](value: T): String = toJson[T](value).toString
}