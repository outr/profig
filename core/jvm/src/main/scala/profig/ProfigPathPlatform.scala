package profig

import fabric.MergeType

import java.io.File
import java.nio.file.{Path, Paths}
import scala.annotation.tailrec
import scala.io.Source
import scala.jdk.CollectionConverters._
import profig.jdk._

trait ProfigPathPlatform {
  this: ProfigPath =>

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
    merge(json, mergeType)
  } catch {
    case t: Throwable => errorHandler.foreach { eh =>
      eh(new RuntimeException(s"Failed to process: $fileName", t))
    }
  }

  def initConfiguration(startPath: Path = Paths.get("."),
                        additionalPaths: List[Path] = Nil,
                        recursiveParents: Boolean = true,
                        includeClassPath: Boolean = true,
                        fileNameMatcher: FileNameMatcher = FileNameMatcher.Default,
                        errorHandler: Option[Throwable => Unit] = None): Unit = {
    Profig.init()
    loadConfiguration(startPath, additionalPaths, recursiveParents, includeClassPath, fileNameMatcher, errorHandler)
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
      val classLoader = getClass.getClassLoader
      files = FileNameMatcher.OverwritePrefixes.toList.flatMap { prefix =>
        FileNameMatcher.DefaultExtensions.toList.flatMap { extension =>
          val fileName = s"$prefix.$extension"
          classLoader.getResources(fileName).asScala.map { url =>
            (fileName, Source.fromURL(url), MergeType.Add)
          }
        }
      } ::: files
      files = FileNameMatcher.AddPrefixes.toList.flatMap { prefix =>
        FileNameMatcher.DefaultExtensions.toList.flatMap { extension =>
          val fileName = s"$prefix.$extension"
          classLoader.getResources(fileName).asScala.map { url =>
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
