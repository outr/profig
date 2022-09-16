package spec

import profig._
import fabric._
import fabric.io.{Format, JsonParser}
import fabric.rw._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.language.implicitConversions

class ProfigSpec extends AnyWordSpec with Matchers {
  "Profig" should {
    "init" in {
      Profig.reset()
      Profig.init()
    }
    "verify classloading not set" in {
      Profig("test.classloading").opt[String] should be(None)
    }
    "verify files not set" in {
      Profig("test.files").opt[String] should be(None)
    }
    "verify `opt` usage" in {
      Profig("test.files").store("yes")
      Profig("test.files").opt[String] should be(Some("yes"))
    }
    "verify `as` with default" in {
      Profig("test.files").asOr[String]("no") should be("yes")
      Profig("test.other").asOr[String]("no") should be("no")
    }
    "merge arguments" in {
      val value = ProfigUtil.args2Json(List("-this.is.an.argument", "Wahoo!"))
      Profig.merge(value)
    }
    "load a String argument" in {
      Profig("this.is.an.argument").as[String] should be("Wahoo!")
      Profig("this")("is")("an")("argument").as[String] should be("Wahoo!")
    }
    "load JVM information from properties" in {
      val info = Profig("java").as[JVMInfo]
      info.specification.vendor should be("Oracle Corporation")
    }
    "store a single String" in {
      Profig("people", "me", "name").store("Matt")
    }
    "load a case class from a path with default arguments" in {
      val person = Profig("people.me").as[Person]
      person should be(Person("Matt", None))
    }
    "storage a case class" in {
      Profig("people", "john").store(Person("John Doe", Some(123)))
    }
    "load the stored case class from path" in {
      val person = Profig("people")("john").as[Person]
      person should be(Person("John Doe", Some(123)))
    }
    "load an optional value that is not there" in {
      val value = Profig("this.does.not.exist").opt[String]
      value should be(None)
    }
    "verify that test.value was loaded" in {
      Profig("test.value").store(true)
      val value = Profig("test.value").opt[Boolean]
      value should be(Some(true))
    }
    "remove a value" in {
      Profig("people.john.age").opt[Int] should be(Some(123))
      Profig("people.john.age").remove()
      Profig("people.john.age").opt[Int] should be(None)
    }
    "add a value back" in {
      Profig("people.john.age").opt[Int] should be(None)
      Profig("people.john.age").store(321)
      Profig("people.john.age").opt[Int] should be(Some(321))
    }
    "see no spill-over in orphaned Profig" in {
      val orphan = Profig.empty
      orphan("people.john.age").opt[Int] should be(None)
    }
    "validate loading a String value of true as Boolean" in {
      Profig("test.boolean").merge(bool(true))
      Profig("test.boolean").as[Boolean] should be(true)
    }
    "validate overwrite" in {
      val profig = Profig.empty
      profig.json should be(obj())
      profig.merge(obj(
        "test" -> "one"
      ), MergeType.Overwrite)
      profig.json should be(obj("test" -> "one"))
      profig.merge(obj(
        "test" -> "two"
      ), MergeType.Overwrite)
      profig.json should be(obj("test" -> "two"))
    }
    "validate add" in {
      val profig = Profig.empty
      profig.json should be(obj())
      profig.merge(obj(
        "test" -> "one"
      ), MergeType.Add)
      profig.json should be(obj("test" -> "one"))
      profig.merge(obj(
        "test" -> "two"
      ), MergeType.Add)
      profig.json should be(obj("test" -> "one"))
    }
    "merge two Json objects" in {
      val json1 = JsonParser(
        """{
          |  "one": 1,
          |  "two": 2,
          |  "three": 3
          |}""".stripMargin, Format.Json)
      val json2 = JsonParser(
        """{
          |  "three": "tres",
          |  "four": "quatro",
          |  "five": "cinco"
          |}""".stripMargin, Format.Json)
      val merged = Json.merge(json1, json2)
      merged should be(obj(
        "one" -> 1,
        "two" -> 2,
        "three" -> "tres",
        "four" -> "quatro",
        "five" -> "cinco"
      ))
    }
    "map values" in {
      val p = Profig.empty
      p.merge(obj(
        "one" -> obj(
          "two" -> obj(
            "three" -> 3
          )
        )
      ))
      p("one").map(
        "two.three" -> "threeValue",
        "two.three.four" -> "fourValue"
      )
      p("one.threeValue").get() should be(Some(num(3)))
    }
    "load a value that doesn't exist from defaults" in {
      val p = Profig("information").as[Information]
    }
  }

  case class Person(name: String, age: Option[Int] = None)

  object Person {
    implicit val rw: RW[Person] = ccRW
  }

  case class JVMInfo(version: String, specification: Specification)

  object JVMInfo {
    implicit val rw: RW[JVMInfo] = ccRW
  }

  case class Specification(vendor: String, name: String, version: String)

  object Specification {
    implicit val rw: RW[Specification] = ccRW
  }
  
  case class Information(description: String = "default")
  
  object Information {
    implicit val rw: RW[Information] = ccRW
  }
}