package spec

import org.scalatest.{Matchers, WordSpec}
import profig.Profig

class ProfigSpec extends WordSpec with Matchers {
  private val instance: Profig = Profig

  "Profig" should {
    "merge arguments" in {
      instance.merge(List("-this.is.an.argument", "Wahoo!"))
    }
    "load a String argument" in {
      instance("this.is.an.argument").as[String] should be("Wahoo!")
    }
    "load JSON arguments" in {
      instance.merge("{ \"this.is.another.argument\" : \"Hola!\" }")
    }
    "load JVM information from properties" in {
      val info = instance("java").as[JVMInfo]
      info.specification.vendor should be("Oracle Corporation")
      info.specification.version should be("1.8")
    }
    "store a single String" in {
      instance("people", "me", "name").store("Matt")
    }
    "load a case class from a path with default arguments" in {
      val person = instance("people.me").as[Person]
      person should be(Person("Matt"))
    }
    "storage a case class" in {
      instance("people", "john").store(Person("John Doe", 123))
    }
    "load the stored case class from path" in {
      val person = instance("people")("john").as[Person]
      person should be(Person("John Doe", 123))
    }
    "load an optional value that is not there" in {
      val value = instance("this.does.not.exist").as[Option[String]]
      value should be(None)
    }
    "verify that test.value was loaded" in {
      val value = instance("test.value").as[Option[Boolean]]
      value should be(Some(true))
    }
    "remove a value" in {
      instance("people.john.age").as[Option[Int]] should be(Some(123))
      instance("people.john.age").remove()
      instance("people.john.age").as[Option[Int]] should be(None)
    }
    "add a value back" in {
      instance("people.john.age").as[Option[Int]] should be(None)
      instance("people.john.age").store(321)
      instance("people.john.age").as[Option[Int]] should be(Some(321))
    }
  }

  case class Person(name: String, age: Int = 21)

  case class JVMInfo(version: String, specification: Specification)

  case class Specification(vendor: String, name: String, version: String)
}