package profig

import java.io.File
import scala.collection.JavaConverters._

object ProfigJVM {
  def init(): Unit = {
    PlatformMacros.defaults.foreach { path =>
      getClass.getClassLoader.getResources(path).asScala.foreach { url =>
        Profig.defaults(url)
      }
    }
    PlatformMacros.defaults.foreach { path =>
      val file = new File(path)
      if (file.exists()) {
        Profig.defaults(file)
      }
    }
    PlatformMacros.paths.foreach { path =>
      getClass.getClassLoader.getResources(path).asScala.foreach { url =>
        Profig.merge(url)
      }
    }
    PlatformMacros.paths.foreach { path =>
      val file = new File(path)
      if (file.exists()) {
        Profig.merge(file)
      }
    }
  }
}
