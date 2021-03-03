package spec

import fabric.rw._

import java.io.File
import profig._

case class Special(title: String, location: File)

object Special {
  implicit def rw: ReaderWriter[Special] = ccRW
}