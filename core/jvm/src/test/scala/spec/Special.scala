package spec

import java.io.File

import profig.Pickler._

case class Special(title: String, location: File)

object Special {
  implicit val fileReadWriter: ReadWriter[File] = readwriter[String].bimap(_.getAbsolutePath, new File(_))

  implicit def rw: ReadWriter[Special] = macroRW
}