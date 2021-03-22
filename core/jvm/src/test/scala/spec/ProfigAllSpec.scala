package spec

import profig._

import fabric._
import testy._

class ProfigAllSpec extends Spec {
  "Profig all" should {
    "initialize" in {
      Profig.init()
    }
    "verify that XML, YAML, and HOCON are available" in {
      ProfigJson.types should be(Set("yaml", "hocon", "json", "config", "conf", "yml", "properties", "xml", "prop", "props"))
    }
    "verify YAML support works" in {
      ProfigJson.Yaml(
        """
          |test:
          |  yaml: "yes"
          |""".stripMargin) should be(obj("test" -> obj("yaml" -> "yes")))
    }
    "verify HOCON support works" in {
      ProfigJson.Hocon(
        """
          |test.hocon = "yes"
          |""".stripMargin) should be(obj("test" -> obj("hocon" -> "yes")))
    }
    "verify XML support works" in {
      ProfigJson.XML(
        """
          |<test><xml>yes</xml></test>""".stripMargin) should be(obj("test" -> obj("xml" -> "yes")))
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