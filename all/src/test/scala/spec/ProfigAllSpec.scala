package spec

import io.circe.Json
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import profig._

class ProfigAllSpec extends AsyncWordSpec with Matchers {
  "Profig all" should {
    "initialize" in {
      Profig.init().map(_ => succeed)
    }
    "verify that XML, YAML, and HOCON are available" in {
      ProfigJson.types should be(Set("yaml", "hocon", "json", "config", "conf", "yml", "properties", "xml", "prop", "props"))
    }
    "verify YAML support works" in {
      ProfigYaml(
        """
          |test:
          |  yaml: "yes"
          |""".stripMargin) should be(Json.obj("test" -> Json.obj("yaml" -> Json.fromString("yes"))))
    }
    "verify HOCON support works" in {
      ProfigHocon(
        """
          |test.hocon = "yes"
          |""".stripMargin) should be(Json.obj("test" -> Json.obj("hocon" -> Json.fromString("yes"))))
    }
    "verify XML support works" in {
      ProfigXML(
        """
          |<test><xml>yes</xml></test>""".stripMargin) should be(Json.obj("test" -> Json.obj("xml" -> Json.fromString("yes"))))
    }
    "load paths" in {
      Profig.loadConfiguration(errorHandler = Some(t => throw t))
      val test = Profig("test")
      test("value").as[Boolean] should be(true)
      test("yaml").as[String] should be("yes")
      test("hocon").as[String] should be("yes")
      test("xml").as[String] should be("yes")
      test("classloader").as[String] should be("yes")
    }
  }
}