package spec

import io.circe.Json
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import profig._

class ProfigSpec extends AsyncWordSpec with Matchers {
  "Profig" should {
    "init" in {
      Profig.init().map(_ => succeed)
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
      Profig.merge(List("-this.is.an.argument", "Wahoo!"))
      succeed
    }
    "load a String argument" in {
      Profig("this.is.an.argument").as[String] should be("Wahoo!")
    }
    "load JVM information from properties" in {
      val info = Profig("java").as[JVMInfo]
      info.specification.vendor should be("Oracle Corporation")
    }
    "store a single String" in {
      Profig("people", "me", "name").store("Matt")
      succeed
    }
    "load a case class from a path with default arguments" in {
      val person = Profig("people.me").as[Person]
      person should be(Person("Matt"))
    }
    "storage a case class" in {
      Profig("people", "john").store(Person("John Doe", 123))
      succeed
    }
    "load the stored case class from path" in {
      val person = Profig("people")("john").as[Person]
      person should be(Person("John Doe", 123))
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
    "create a child Profig Profig" in {
      val child = Profig.child()
      child("child.info").store("Child Local")
      Profig("child.info").opt[String] should be(None)
      child("child.info").opt[String] should be(Some("Child Local"))
    }
    "update parent and see in the child" in {
      val child = Profig.child()
      child("people.john.age").opt[Int] should be(Some(321))
      Profig("people.john.age").opt[Int] should be(Some(321))
      child("people.john.age").store(1234)
      Profig("people.john.age").opt[Int] should be(Some(321))
      child("people.john.age").opt[Int] should be(Some(1234))
      child("people.john.age").remove()
      child("people.john.age").opt[Int] should be(Some(321))
      Profig("people.john.age").opt[Int] should be(Some(321))
    }
    "see no spill-over in orphaned Profig" in {
      val orphan = Profig(None)
      orphan("people.john.age").opt[Int] should be(None)
    }
    "compile-time Json parsing" in {
      val parsed = MacroTest.format("""{"name": "John Doe", "age": 1234}""")
      parsed should be("""{
                         |  "name" : "John Doe",
                         |  "age" : 1234
                         |}""".stripMargin)
    }
    "validate loading a String value of true as Boolean" in {
      Profig("test.boolean").merge(Json.fromString("true"))
      Profig("test.boolean").as[Boolean] should be(true)
    }
    "validate overwrite" in {
      val profig = Profig.empty
      profig.json should be(Json.obj())
      profig.merge(Json.obj(
        "test" -> Json.fromString("one")
      ), MergeType.Overwrite)
      profig.json should be(Json.obj("test" -> Json.fromString("one")))
      profig.merge(Json.obj(
        "test" -> Json.fromString("two")
      ), MergeType.Overwrite)
      profig.json should be(Json.obj("test" -> Json.fromString("two")))
    }
    "validate add" in {
      val profig = Profig.empty
      profig.json should be(Json.obj())
      profig.merge(Json.obj(
        "test" -> Json.fromString("one")
      ), MergeType.Add)
      profig.json should be(Json.obj("test" -> Json.fromString("one")))
      profig.merge(Json.obj(
        "test" -> Json.fromString("two")
      ), MergeType.Add)
      profig.json should be(Json.obj("test" -> Json.fromString("one")))
    }
  }

  case class Person(name: String, age: Int = 21)

  case class JVMInfo(version: String, specification: Specification)

  case class Specification(vendor: String, name: String, version: String)
}