package spec

import java.io.File

import io.circe.Decoder.Result
import io.circe._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import profig.Profig

class ProfigJVMSpec extends AnyWordSpec with Matchers {
  "Profig JVM" should {
    "merge a special type" in {
      import Special.fileDecoder        // For the as[File]

      val location = new File(System.getProperty("user.home"))
      Profig("special").store(Special("testing", location))
      Profig("special.title").as[String] should be("testing")
      Profig("special.location").as[File] should be(location)
    }
    "load a special type" in {
      val special = Profig("special").as[Special]
      special.title should be("testing")
      special.location should be(new File(System.getProperty("user.home")))
    }
  }
}

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