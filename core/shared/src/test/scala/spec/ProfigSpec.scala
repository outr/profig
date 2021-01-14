package spec

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import profig._
import profig.Pickler._

class ProfigSpec extends AsyncWordSpec with Matchers {
  "Profig" should {
    "init" in {
      Profig.init()
      succeed
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
      Profig("this")("is")("an")("argument").as[String] should be("Wahoo!")
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
      person should be(Person("Matt", None))
    }
    "storage a case class" in {
      Profig("people", "john").store(Person("John Doe", Some(123)))
      succeed
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
      Profig("test.boolean").merge(Json.parse("true"))
      Profig("test.boolean").as[Boolean] should be(true)
    }
    "validate overwrite" in {
      val profig = Profig.empty
      profig.json should be(Json())
      profig.merge(Json.obj(
        "test" -> Json.string("one")
      ), MergeType.Overwrite)
      profig.json should be(Json.obj("test" -> Json.string("one")))
      profig.merge(Json.obj(
        "test" -> Json.string("two")
      ), MergeType.Overwrite)
      profig.json should be(Json.obj("test" -> Json.string("two")))
    }
    "validate add" in {
      val profig = Profig.empty
      profig.json should be(Json.obj())
      profig.merge(Json.obj(
        "test" -> Json.string("one")
      ), MergeType.Add)
      profig.json should be(Json.obj("test" -> Json.string("one")))
      profig.merge(Json.obj(
        "test" -> Json.string("two")
      ), MergeType.Add)
      profig.json should be(Json.obj("test" -> Json.string("one")))
    }
  }

  case class Person(name: String, age: Option[Int] = None)

  object Person {
    implicit def rw: ReadWriter[Person] = macroRW
  }

  case class JVMInfo(version: String, specification: Specification)

  object JVMInfo {
    implicit def rw: ReadWriter[JVMInfo] = macroRW
  }

  case class Specification(vendor: String, name: String, version: String)

  object Specification {
    implicit def rw: ReadWriter[Specification] = macroRW
  }
}