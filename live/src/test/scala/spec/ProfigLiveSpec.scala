package spec

import java.nio.file.{Files, Paths}

import io.circe.Json
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import profig.{LiveConfig, Profig, ProfigJson, ProfigLive}

class ProfigLiveSpec extends AsyncWordSpec with Matchers {
  "ProfigLive" should {
    val file1 = Paths.get("live-example1.json")

    "init Profig" in {
      Profig.init().map(_ => succeed)
    }
    "create a mutable live var" in {
      val v = ProfigLive.mutable[LiveExample1](LiveExample1(), LiveConfig(file1))
      Files.exists(file1) should be(false)
      v @= LiveExample1("Hello, World!")
      Files.exists(file1) should be(true)
      val s1 = new String(Files.readAllBytes(file1), "UTF-8")
      ProfigJson.Circe(s1) should be(Json.obj("name" -> Json.fromString("Hello, World!")))
      // TODO: write to file and detect changes
    }
    "delete files" in {
      Files.delete(file1)
      succeed
    }
  }
}

case class LiveExample1(name: String = "")