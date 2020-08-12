import java.io.File
import java.net.URL
import java.nio.file.{Path, Paths}

import io.circe.Json
import moduload.Moduload

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.io.Source
import scala.language.implicitConversions
import scala.jdk.CollectionConverters._

package object profig extends SharedJSONConversions {
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

  def initProfig(loadModules: Boolean)(implicit ec: ExecutionContext): Future[Unit] = {
    if (loadModules) {
      Moduload.load()
    } else {
      Future.successful(())
    }
  }

  implicit class ProfigPathJVM(val profigPath: ProfigPath) extends AnyVal {
    def loadFile(file: File,
                 mergeType: MergeType = MergeType.Overwrite,
                 errorHandler: Option[Throwable => Unit] = None): Unit = {
      load(file.getName, Source.fromFile(file), mergeType, errorHandler)
    }

    def load(fileName: String,
             source: Source,
             mergeType: MergeType = MergeType.Overwrite,
             errorHandler: Option[Throwable => Unit] = None): Unit = try {
      val json = source2Json(source, Some(fileName))
      profigPath.merge(json, mergeType)
    } catch {
      case t: Throwable => errorHandler.foreach { eh =>
        eh(new RuntimeException(s"Failed to process: $fileName", t))
      }
    }

    def loadConfiguration(startPath: Path = Paths.get("."),
                          additionalPaths: List[Path] = Nil,
                          recursiveParents: Boolean = true,
                          includeClassPath: Boolean = true,
                          fileNameMatcher: FileNameMatcher = FileNameMatcher.Default,
                          errorHandler: Option[Throwable => Unit] = None): Unit = {
      assert(Profig.isLoaded, "loadConfiguration cannot be executed without first initializing Profig (Profig.init())")

      var files = List.empty[(String, Source, MergeType)]

      @tailrec
      def explorePath(directory: File, recursive: Boolean): Unit = {
        files = files ::: directory.listFiles().toList.filter(_.isFile).flatMap { f =>
          val fileName = f.getName.toLowerCase
          val dot = fileName.indexOf('.')
          val (prefix, extension) = if (dot == -1) {
            (fileName, "")
          } else {
            (fileName.substring(0, dot), fileName.substring(dot + 1))
          }
          fileNameMatcher.matches(prefix, extension).map(t => (f.getName, Source.fromFile(f), t))
        }
        if (recursive) {
          Option(directory.getParentFile) match {
            case None => // Finished
            case Some(parent) => explorePath(parent, recursive)
          }
        }
      }

      // Files
      explorePath(startPath.toFile, recursiveParents)
      additionalPaths.foreach(p => explorePath(p.toFile, recursive = false))

      // ClassLoader
      if (includeClassPath) {
        val classLoader = getClass().getClassLoader
        files = FileNameMatcher.OverwritePrefixes.toList.flatMap { prefix =>
          FileNameMatcher.DefaultExtensions.toList.flatMap { extension =>
            classLoader.getResources(s"$prefix.$extension").asScala.map { url =>
              val fileName = Paths.get(url.toURI.getPath).getFileName.toString
              (fileName, Source.fromURL(url), MergeType.Overwrite)
            }
          }
        } ::: files
        files = FileNameMatcher.AddPrefixes.toList.flatMap { prefix =>
          FileNameMatcher.DefaultExtensions.toList.flatMap { extension =>
            classLoader.getResources(s"$prefix.$extension").asScala.map { url =>
              val fileName = Paths.get(url.toURI.getPath).getFileName.toString
              (fileName, Source.fromURL(url), MergeType.Add)
            }
          }
        } ::: files
      }

      // Process files
      files.foreach {
        case (fileName, source, mergeType) => load(fileName, source, mergeType, errorHandler)
      }
    }
  }

  trait FileNameMatcher {
    def matches(prefix: String, extension: String): Option[MergeType]
  }

  object FileNameMatcher {
    var OverwritePrefixes: Set[String] = Set("config", "configuration", "app", "application")
    var AddPrefixes: Set[String] = Set("default", "defaults")
    var DefaultExtensions: Set[String] = Set("json", "properties", "conf", "config")

    case object Default extends FileNameMatcher {
      override def matches(prefix: String, extension: String): Option[MergeType] = {
        if (DefaultExtensions.contains(extension)) {
          if (OverwritePrefixes.contains(prefix)) {
            Some(MergeType.Overwrite)
          } else if (AddPrefixes.contains(prefix)) {
            Some(MergeType.Add)
          } else {
            None
          }
        } else {
          None
        }
      }
    }
  }
}