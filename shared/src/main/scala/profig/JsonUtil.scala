package profig

import io.circe.Json

import scala.language.experimental.macros

object JsonUtil extends JsonUtil(convertSnake = false)

class JsonUtil(val convertSnake: Boolean) {
  def fromJson[T](json: Json): T = macro Macros.fromJson[T]
  def toJson[T](value: T): Json = macro Macros.toJson[T]

  def fromJsonString[T](jsonString: String): T = macro Macros.fromJsonString[T]
  def toJsonString[T](value: T): String = macro Macros.toJsonString[T]
}