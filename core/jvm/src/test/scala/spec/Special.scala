package spec

import java.io.File

import profig._

case class Special(title: String, location: File)

object Special {
  implicit def rw: ReadWriter[Special] = macroRW
}