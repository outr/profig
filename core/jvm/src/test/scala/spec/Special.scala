package spec

import fabric.rw._

import java.io.File

case class Special(title: String, location: File)

object Special {
  implicit def fileRW: RW[File] = profig.fileReadWriter
  implicit def rw: RW[Special] = ccRW
}