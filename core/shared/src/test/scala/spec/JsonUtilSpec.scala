//package spec
//
//import io.circe.Decoder
//import org.scalatest.{Matchers, WordSpec}
//
//class JsonUtilSpec extends WordSpec with Matchers {
//  "JsonUtil" should {
//    "use implicit decoder" in {
//      import profig.JsonUtil._
//
//      val decoder: Decoder[Test] = implicitly[Decoder[Test]]
//      decoder should not be null
//    }
//  }
//
//  case class Test(name: String)
//}