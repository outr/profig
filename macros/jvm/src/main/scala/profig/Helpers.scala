//package profig
//
//import java.io.File
//import java.nio.file.{Path, Paths}
//
//import io.circe.Decoder.Result
//import io.circe._
//
///**
//  * Common implicits for useful for configurations. This can be mixed-in as a trait in the companion object in order to
//  * allow automatic resolution or imported where necessary.
//  */
//object Helpers extends Helpers
//
//trait Helpers {
//  implicit val fileEncoder: Encoder[File] = new Encoder[File] {
//    override def apply(a: File): Json = Json.fromString(a.getAbsolutePath)
//  }
//  implicit val fileDecoder: Decoder[File] = new Decoder[File] {
//    override def apply(c: HCursor): Result[File] = c.value.asString match {
//      case Some(s) => Right(new File(s))
//      case None => Left(DecodingFailure("Cannot decode a File from null", c.history))
//    }
//  }
//
//  implicit val pathEncoder: Encoder[Path] = new Encoder[Path] {
//    override def apply(a: Path): Json = Json.fromString(a.toAbsolutePath.toString)
//  }
//  implicit val pathDecoder: Decoder[Path] = new Decoder[Path] {
//    override def apply(c: HCursor): Result[Path] = c.value.asString match {
//      case Some(s) => Right(Paths.get(s))
//      case None => Left(DecodingFailure("Cannot decode a Path from null", c.history))
//    }
//  }
//}