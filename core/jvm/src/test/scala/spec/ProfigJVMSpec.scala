package spec

import java.io.File

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import profig._

class ProfigJVMSpec extends AnyWordSpec with Matchers {
  "Profig JVM" should {
    "merge a special type" in {
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
}