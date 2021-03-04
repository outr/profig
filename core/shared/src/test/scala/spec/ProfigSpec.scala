package spec

import profig._
import fabric._
import fabric.parse.Json
import fabric.rw._
import testy._

import scala.language.implicitConversions

class ProfigSpec extends Spec {
  "Profig" should {
    "init" in {
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
      Profig("test.files").as[String]("no") should be("yes")
      Profig("test.other").as[String]("no") should be("no")
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
      val json1 = Json.parse(
        """{
          |  "one": 1,
          |  "two": 2,
          |  "three": 3
          |}""".stripMargin)
      val json2 = Json.parse(
        """{
          |  "three": "tres",
          |  "four": "quatro",
          |  "five": "cinco"
          |}""".stripMargin
      )
      val merged = Value.merge(json1, json2)
      merged should be(obj(
        "one" -> 1,
        "two" -> 2,
        "three" -> "tres",
        "four" -> "quatro",
        "five" -> "cinco"
      ))
    }
  }

  case class Person(name: String, age: Option[Int] = None)

  object Person {
    implicit def rw: ReaderWriter[Person] = ccRW
  }

  case class JVMInfo(version: String, specification: Specification)

  object JVMInfo {
    implicit def rw: ReaderWriter[JVMInfo] = ccRW
  }

  case class Specification(vendor: String, name: String, version: String)

  object Specification {
    implicit def rw: ReaderWriter[Specification] = ccRW
  }
}