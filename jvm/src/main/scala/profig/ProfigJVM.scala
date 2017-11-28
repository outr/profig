package profig

import java.io.File
import scala.collection.JavaConverters._

object ProfigJVM {
  def init(): Unit = {
    PlatformMacros.defaults.foreach { path =>
      getClass.getClassLoader.getResources(path).asScala.foreach { url =>
        Config.defaults(url)
      }
    }
    PlatformMacros.defaults.foreach { path =>
      val file = new File(path)
      if (file.exists()) {
        Config.defaults(file)
      }
    }
    PlatformMacros.paths.foreach { path =>
      getClass.getClassLoader.getResources(path).asScala.foreach { url =>
        Config.merge(url)
      }
    }
    PlatformMacros.paths.foreach { path =>
      val file = new File(path)
      if (file.exists()) {
        Config.merge(file)
      }
    }
  }
}
