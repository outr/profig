package spec

import org.scalatest.{Matchers, WordSpec}
import profig.Profig

class ProfigSpec extends WordSpec with Matchers {
  "Profig" should {
    "initialize" in {
      Profig.init(List("-this.is.an.argument", "Wahoo!"))
    }
    "load a String argument" in {
      Profig("this.is.an.argument").as[String] should be("Wahoo!")
    }
    "load JSON arguments" in {
      Profig.merge("{ \"this.is.another.argument\" : \"Hola!\" }")
    }
    "load JVM information from properties" in {
      val info = Profig("java").as[JVMInfo]
      info.specification.vendor should be("Oracle Corporation")
      info.specification.version should be("1.8")
    }
    "store a single String" in {
      Profig("people", "me", "name").store("Matt")
    }
    "load a case class from a path with default arguments" in {
      val person = Profig("people.me").as[Person]
      person should be(Person("Matt"))
    }
    "storage a case class" in {
      Profig("people", "john").store(Person("John Doe", 123))
    }
    "load the stored case class from path" in {
      val person = Profig("people")("john").as[Person]
      person should be(Person("John Doe", 123))
    }
    "load an optional value that is not there" in {
      val value = Profig("this.does.not.exist").as[Option[String]]
      value should be(None)
    }
    "verify that test.value was loaded" in {
      val value = Profig("test.value").as[Option[Boolean]]
      value should be(Some(true))
    }
    "remove a value" in {
      Profig("people.john.age").as[Option[Int]] should be(Some(123))
      Profig("people.john.age").remove()
      Profig("people.john.age").as[Option[Int]] should be(None)
    }
    "add a value back" in {
      Profig("people.john.age").as[Option[Int]] should be(None)
      Profig("people.john.age").store(321)
      Profig("people.john.age").as[Option[Int]] should be(Some(321))
    }
  }
}

case class Person(name: String, age: Int = 21)

case class JVMInfo(version: String, specification: Specification)

case class Specification(vendor: String, name: String, version: String)