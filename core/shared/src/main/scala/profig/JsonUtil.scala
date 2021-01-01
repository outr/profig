package profig

import io.circe.{Decoder, Encoder, Json, Printer}

import scala.language.experimental.macros

object JsonUtil {
  def fromJson[T](json: Json)(implicit decoder: Decoder[T]): T = decoder.decodeJson(json) match {
    case Left(failure) => throw failure
    case Right(t) => t
  }
  def toJson[T](value: T)(implicit encoder: Encoder[T]): Json = encoder(value)

  def fromJsonString[T](jsonString: String)(implicit decoder: Decoder[T]): T = JsonParser.parse(jsonString) match {
    case Left(failure) => throw failure
    case Right(json) => fromJson[T](json)
  }
  def toJsonString[T](value: T, printer: Printer = Printer.noSpaces)
                     (implicit encoder: Encoder[T]): String = toJson[T](value).printWith(printer)
}