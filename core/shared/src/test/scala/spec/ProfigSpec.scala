package spec

import org.scalatest.{Matchers, WordSpec}
import profig.{FileType, Profig}

class ProfigSpec extends WordSpec with Matchers {
  "Profig" should {
    "verify classloading not set" in {
      Profig("test.classloading").as[Option[String]] should be(None)
    }
    "verify files not set" in {
      Profig("test.files").as[Option[String]] should be(None)
    }
    "load configuration files" in {
      Profig.loadDefaults()
    }
    "verify classloading" in {
      Profig("test.classloading").as[Option[String]] should be(Some("yes"))
    }
    "verify files" in {
      Profig("test.files").as[Option[String]] should be(Some("yes"))
    }
    "verify `opt` usage" in {
      Profig("test.files").opt[String] should be(Some("yes"))
    }
    "verify `as` with default" in {
      Profig("test.files").as[String]("no") should be("yes")
      Profig("test.other").as[String]("no") should be("no")
    }
    "merge arguments" in {
      Profig.merge(List("-this.is.an.argument", "Wahoo!"))
    }
    "load a String argument" in {
      Profig("this.is.an.argument").as[String] should be("Wahoo!")
    }
    "load JSON arguments" in {
      Profig.merge("{ \"this.is.another.argument\" : \"Hola!\" }", FileType.Json)
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
    "create a child Profig Profig" in {
      val child = Profig.child()
      child("child.info").store("Child Local")
      Profig("child.info").as[Option[String]] should be(None)
      child("child.info").as[Option[String]] should be(Some("Child Local"))
    }
    "update parent and see in the child" in {
      val child = Profig.child()
      child("people.john.age").as[Option[Int]] should be(Some(321))
      Profig("people.john.age").as[Option[Int]] should be(Some(321))
      child("people.john.age").store(1234)
      Profig("people.john.age").as[Option[Int]] should be(Some(321))
      child("people.john.age").as[Option[Int]] should be(Some(1234))
      child("people.john.age").remove()
      child("people.john.age").as[Option[Int]] should be(Some(321))
      Profig("people.john.age").as[Option[Int]] should be(Some(321))
    }
    "see no spill-over in orphaned Profig" in {
      val orphan = Profig(None)
      orphan("people.john.age").as[Option[Int]] should be(None)
    }
    "verify YAML support works" in {
      Profig("test.yaml").as[Option[String]] should be(Some("yes"))
    }
    "verify HOCON support works" in {
      Profig("test.hocon").as[Option[String]] should be(Some("yes"))
    }
    "verify XML support works" in {
      Profig("test.xml").as[Option[String]] should be(Some("yes"))
    }
    "compile-time Json parsing" in {
      val parsed = MacroTest.format("""{"name": "John Doe", "age": 1234}""")
      parsed should be("""{
                         |  "name" : "John Doe",
                         |  "age" : 1234
                         |}""".stripMargin)
    }
  }

  case class Person(name: String, age: Int = 21)

  case class JVMInfo(version: String, specification: Specification)

  case class Specification(vendor: String, name: String, version: String)
}