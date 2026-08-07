package profig

import java.io.File
import java.net.URL
import java.nio.file.Path
import scala.io.Source
import fabric._

import scala.language.implicitConversions

trait PlatformPickler {
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

  def initProfig(profig: Profig): Unit = {
    // Referencing ProfigJson registers the extra file extensions (yaml, xml, hocon, etc.) before the scan below
    ProfigJson.types
    profig.loadConfiguration(errorHandler = Some { t =>
      System.err.println(s"Profig failed to load configuration file: ${t.getMessage}")
    })
  }

  /**
    * Best-effort detection of the application's command-line arguments via the `sun.java.command` system property.
    * Arguments containing spaces will be split incorrectly; use `Profig.merge(args)` for exact control.
    */
  def defaultArguments: List[String] = Option(System.getProperty("sun.java.command")).map(_.trim) match {
    case Some(command) if command.nonEmpty => command.split("""\s+""").toList.drop(1)
    case _ => Nil
  }
}