package spec

import fabric.JsonPath
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.io.File
import profig._

class ProfigJVMSpec extends AnyWordSpec with Matchers {
  "Profig JVM" should {
    "init" in {
      Profig.reset()
      Profig.init()
    }
    "merge a special type" in {
      val location = new File(System.getProperty("user.home"))
      Profig("special").store(Special("testing", location))
      Profig(JsonPath.parse("special.title")).as[String] should be("testing")
      Profig(JsonPath.parse("special.location")).as[File] should be(location)
    }
    "load a special type" in {
      val special = Profig("special").as[Special]
      special.title should be("testing")
      special.location should be(new File(System.getProperty("user.home")))
    }
  }
}