package profig

import profig.Pickler._

import java.io.File

trait PlatformPickler {
  implicit val fileReadWriter: ReadWriter[File] = readwriter[String].bimap(_.getAbsolutePath, new File(_))
}