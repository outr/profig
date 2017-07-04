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
  }
}

case class Person(name: String, age: Int = 21)