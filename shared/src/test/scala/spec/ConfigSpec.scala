package spec

import org.scalatest.{Matchers, WordSpec}
import profig.Config

class ConfigSpec extends WordSpec with Matchers {
  "Config" should {
    "initialize properly" in {
      Config.init(List("-this.is.an.argument", "Wahoo!"))
    }
    "store a single String" in {
      Config.store[String]("Matt", "people.me.name")
    }
    "load a case class from a path with default arguments" in {
      val person = Config.as[Person]("people.me")
      person should be(Person("Matt"))
    }
  }
}

case class Person(name: String, age: Int = 21)