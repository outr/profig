package spec

import hierarchical._
import hierarchical.rw._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import profig._

class JsonSpec extends AnyWordSpec with Matchers {
  "Json" should {
    "convert an empty JSON doc to a case class with default arguments" in {
      val d = obj().as[Defaults]
      d should be(Defaults())
    }
  }

  case class Defaults(name: String = "John Doe", age: Int = 21)

  object Defaults {
    implicit val rw: ReaderWriter[Defaults] = ccRW
  }
}