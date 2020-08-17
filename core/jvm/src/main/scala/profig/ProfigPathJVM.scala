package profig

import java.io.File
import java.nio.file.{Path, Paths}

import scala.annotation.tailrec
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.io.Source
import scala.jdk.CollectionConverters._

class ProfigPathJVM(val profigPath: ProfigPath) extends AnyVal {
  def loadFile(file: File,
               mergeType: MergeType = MergeType.Overwrite,
               errorHandler: Option[Throwable => Unit] = None): Unit = {
    load(file.getName, Source.fromFile(file), mergeType, errorHandler)
  }

  def load(fileName: String,
           source: Source,
           mergeType: MergeType = MergeType.Overwrite,
           errorHandler: Option[Throwable => Unit] = None): Unit = try {
    println(s"Loading config: $fileName:${mergeType} ($source)...")
    val json = source2Json(source, Some(fileName))
    profigPath.merge(json, mergeType)
  } catch {
    case t: Throwable => errorHandler.foreach { eh =>
      eh(new RuntimeException(s"Failed to process: $fileName", t))
    }
  }

  def initConfigurationBlocking(startPath: Path = Paths.get("."),
                                additionalPaths: List[Path] = Nil,
                                recursiveParents: Boolean = true,
                                includeClassPath: Boolean = true,
                                fileNameMatcher: FileNameMatcher = FileNameMatcher.Default,
                                errorHandler: Option[Throwable => Unit] = None): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val future = initConfiguration(startPath, additionalPaths, recursiveParents, includeClassPath, fileNameMatcher, errorHandler)
    Await.result(future, Duration.Inf)
  }

  def initConfiguration(startPath: Path = Paths.get("."),
                        additionalPaths: List[Path] = Nil,
                        recursiveParents: Boolean = true,
                        includeClassPath: Boolean = true,
                        fileNameMatcher: FileNameMatcher = FileNameMatcher.Default,
                        errorHandler: Option[Throwable => Unit] = None)
                       (implicit ec: ExecutionContext): Future[Unit] = {
    Profig.init().map { _ =>
      loadConfiguration(startPath, additionalPaths, recursiveParents, includeClassPath, fileNameMatcher, errorHandler)
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