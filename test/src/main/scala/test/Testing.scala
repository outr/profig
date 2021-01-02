package test

import upickle.default.{macroRW, ReadWriter => RW}
import upickle.default._

object Testing {
  def main(args: Array[String]): Unit = {
    implicit def rw: RW[Person] = macroRW

    val s =
      """{
        | "name": "Matt"
        |}""".stripMargin

//    println(read[Person](s))
    println(as[Person](s))
  }

  def as[T](s: String)(implicit reader: Reader[T] = null): T = {
    println(s"Reader? ${reader}")
    read[T](s)
  }
}

case class Person(name: String, age: Int = 21)