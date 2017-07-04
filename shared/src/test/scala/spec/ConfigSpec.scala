package spec

import org.scalatest.{Matchers, WordSpec}
import profig.Config

class ConfigSpec extends WordSpec with Matchers {
  "Config" should {
    "initialize properly" in {
      Config.init(List("-this.is.an.argument", "Wahoo!"))
    }
    "load a String argument" in {
      Config.as[String]("this.is.an.argument") should be("Wahoo!")
    }
    "store a single String" in {
      Config.store("Matt", "people.me.name")
    }
    "load a case class from a path with default arguments" in {
      val person = Config.as[Person]("people.me")
      person should be(Person("Matt"))
    }
    "storage a case class" in {
      Config.store(Person("John Doe", 123), "people.john")
    }
    "load the stored case class from path" in {
      val person = Config.as[Person]("people.john")
      person should be(Person("John Doe", 123))
    }
  }
}

case class Person(name: String, age: Int = 21)