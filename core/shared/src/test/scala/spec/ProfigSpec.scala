package spec

import java.io.File

import io.circe.Decoder.Result
import io.circe._
import org.scalatest.{Matchers, WordSpec}
import profig.{ConfigType, ConfigurationPath, Profig}

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
    "merge arguments" in {
      Profig.merge(List("-this.is.an.argument", "Wahoo!"))
    }
    "load a String argument" in {
      Profig("this.is.an.argument").as[String] should be("Wahoo!")
    }
    "load JSON arguments" in {
      Profig.merge("{ \"this.is.another.argument\" : \"Hola!\" }", ConfigType.Json)
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
    "merge a special type" in {
      import Special.fileDecoder        // For the as[File]

      val location = new File(System.getProperty("user.home"))
      Profig("special").store(Special("testing", location))
      Profig("special.title").as[String] should be("testing")
      Profig("special.location").as[File] should be(location)
    }
    "load a special type" in {
      val special = Profig("special").as[Special]
      special.title should be("testing")
      special.location should be(new File(System.getProperty("user.home")))
    }
  }

  case class Person(name: String, age: Int = 21)

  case class JVMInfo(version: String, specification: Specification)

  case class Specification(vendor: String, name: String, version: String)
}

case class Special(title: String, location: File)

object Special {
  implicit val fileEncoder: Encoder[File] = new Encoder[File] {
    override def apply(a: File): Json = Json.fromString(a.getAbsolutePath)
  }
  implicit val fileDecoder: Decoder[File] = new Decoder[File] {
    override def apply(c: HCursor): Result[File] = c.value.asString match {
      case Some(s) => Right(new File(s))
      case None => Left(DecodingFailure("Cannot decode a File from null", c.history))
    }
  }
}