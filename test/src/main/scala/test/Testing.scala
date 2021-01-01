package test

import upickle.default.{macroRW, ReadWriter => RW}
import upickle.default._

object Testing {
  def main(args: Array[String]): Unit = {
    @inline implicit def rw[T]: RW[T] = macroRW

    val s =
      """{
        | "name": "Matt"
        |}""".stripMargin

    println(read[Person](s))
  }
}

case class Person(name: String, age: Int = 21)