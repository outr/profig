package profig

import io.circe.Json

import scala.language.experimental.macros

object JsonUtil {
  def fromJson[T](json: Json): T = macro Macros.fromJson[T]
  def toJson[T](value: T): Json = macro Macros.toJson[T]

  def fromJsonString[T](jsonString: String): T = macro Macros.fromJsonString[T]
  def toJsonString[T](value: T): String = macro Macros.toJsonString[T]

  def decoder[T]: io.circe.Decoder[T] = macro Macros.decoder[T]
  def encoder[T]: io.circe.Encoder[T] = macro Macros.encoder[T]

  // TODO: fix this
  implicit def exportedDecoder[T]: io.circe.export.Exported[io.circe.Decoder[T]] = macro Macros.exportedDecoder[T]
  implicit def exportedEncoder[T]: io.circe.export.Exported[io.circe.Encoder[T]] = macro Macros.exportedEncoder[T]
}