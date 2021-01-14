package spec

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.{AnyWordSpec, AsyncWordSpec}
import profig._

class ProfigAllSpec extends AnyWordSpec with Matchers {
  "Profig all" should {
    "initialize" in {
      Profig.init()
      succeed
    }
    "verify that XML, YAML, and HOCON are available" in {
      ProfigJson.types should be(Set("yaml", "hocon", "json", "config", "conf", "yml", "properties", "xml", "prop", "props"))
    }
    "verify YAML support works" in {
      ProfigYaml(
        """
          |test:
          |  yaml: "yes"
          |""".stripMargin) should be(Json.obj("test" -> Json.obj("yaml" -> Json.string("yes"))))
    }
    "verify HOCON support works" in {
      ProfigHocon(
        """
          |test.hocon = "yes"
          |""".stripMargin) should be(Json.obj("test" -> Json.obj("hocon" -> Json.string("yes"))))
    }
    "verify XML support works" in {
      ProfigXML(
        """
          |<test><xml>yes</xml></test>""".stripMargin) should be(Json.obj("test" -> Json.obj("xml" -> Json.string("yes"))))
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