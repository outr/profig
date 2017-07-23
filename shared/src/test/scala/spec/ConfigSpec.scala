package spec

import org.scalatest.{Matchers, WordSpec}
import profig.Config

class ConfigSpec extends WordSpec with Matchers {
  "Config" should {
    "load arguments" in {
      Config.merge(List("-this.is.an.argument", "Wahoo!"))
    }
    "load a String argument" in {
      Config("this.is.an.argument").as[String] should be("Wahoo!")
    }
    "load JSON arguments" in {
      Config.merge("{ \"this.is.another.argument\" : \"Hola!\" }")
    }
    "load JVM information from properties" in {
      val info = Config("java").as[JVMInfo]
      info.specification.vendor should be("Oracle Corporation")
      info.specification.version should be("1.8")
    }
    "store a single String" in {
      Config("people", "me", "name").store("Matt")
    }
    "load a case class from a path with default arguments" in {
      val person = Config("people.me").as[Person]
      person should be(Person("Matt"))
    }
    "storage a case class" in {
      Config("people", "john").store(Person("John Doe", 123))
    }
    "load the stored case class from path" in {
      val person = Config("people")("john").as[Person]
      person should be(Person("John Doe", 123))
    }
    "load an optional value that is not there" in {
      val value = Config("this.does.not.exist").as[Option[String]]
      value should be(None)
    }
  }
}

case class Person(name: String, age: Int = 21)

case class JVMInfo(version: String, specification: Specification)

case class Specification(vendor: String, name: String, version: String)