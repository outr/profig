package profig

import moduload.Moduload

import java.io.File
import java.net.URL
import java.nio.file.Path
import scala.io.Source

import scala.language.implicitConversions

trait PlatformPickler {
  implicit val fileReadWriter: ReadWriter[File] = readwriter[String].bimap(_.getAbsolutePath, new File(_))

  implicit def path2JSON(path: Path): Json = file2JSON(path.toFile)
  implicit def file2JSON(file: File): Json = source2Json(Source.fromFile(file), Some(file.getName))
  implicit def url2JSON(url: URL): Json = source2Json(Source.fromURL(url), Some(url.getFile))
  implicit def source2JSON(source: Source): Json = source2Json(source, None)

  def source2Json(source: Source, fileName: Option[String]): Json = {
    val extension = fileName.flatMap { fn =>
      val index = fn.lastIndexOf('.')
      if (index != -1) {
        Some(fn.substring(index + 1).toLowerCase)
      } else {
        None
      }
    }
    val s = source2String(source)
    ProfigJson(s, `extension`)
  }

  private def source2String(source: Source): String = try {
    source.mkString
  } finally {
    source.close()
  }

  def initProfig(loadModules: Boolean): Unit = {
    if (loadModules) {
      Moduload.load()
    }
  }
}