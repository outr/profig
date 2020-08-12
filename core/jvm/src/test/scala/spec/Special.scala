package spec

import java.io.File
import io.circe.Decoder.Result
import io.circe._

case class Special(title: String, location: File)

object Special {
  implicit val fileEncoder: Encoder[File] = new Encoder[File] {
    override def apply(a: File): Json = Json.fromString(a.getAbsolutePath)
  }
  implicit val fileDecoder: Decoder[File] = new Decoder[File] {
    override def apply(c: HCursor): Result[File] = c.value.asString match {
      case Some(s) => Right(new File(s))
      case None => Left(DecodingFailure("Cannot decode a File from null", c.history))
    }
  }
}