package spec

import org.scalatest.{AsyncWordSpec, Matchers, WordSpec}
import profig.input.{ProfigInput, ProvidedProfigInput}

class ProfigInputSpec extends AsyncWordSpec with Matchers {
  "ProfigInput" should {
    "extract InputData from Person" in {
      val input = ProfigInput.createInputData[Person]
      input.arguments.size should be(2)
      input.arguments.head.convert("Test") should be(Some("Test"))
      input.arguments(1).convert("5") should be(Some(5))
      input.arguments(1).convert("TEST") should be(None)
      input.arguments(1).convertOrDefault("TEST") should be(Some(21))
      val p = input.create(Vector("One", 2))
      p.name should be("One")
      p.age should be(2)
    }
    "extract Person from existing data" in {
      val input = ProfigInput.createInputData[Person]
      val provided = new ProvidedProfigInput(List("John Doe", ""))
      provided.read(input).map { pOption =>
        val p = pOption.getOrElse(fail())
        p.name should be("John Doe")
        p.age should be(21)
      }
    }
  }
}

case class Person(name: String, age: Int = 21)