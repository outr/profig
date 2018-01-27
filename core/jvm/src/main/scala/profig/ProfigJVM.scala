package profig

import java.io.File

import scala.collection.JavaConverters._

object ProfigJVM {
  def init(instance: Profig): Unit = {
    PlatformMacros.defaults.foreach { path =>
      getClass.getClassLoader.getResources(path).asScala.foreach { url =>
        instance.defaults(url)
      }
    }
    PlatformMacros.defaults.foreach { path =>
      val file = new File(path)
      if (file.exists()) {
        instance.defaults(file)
      }
    }
    PlatformMacros.paths.foreach { path =>
      getClass.getClassLoader.getResources(path).asScala.foreach { url =>
        instance.merge(url)
      }
    }
    PlatformMacros.paths.foreach { path =>
      val file = new File(path)
      if (file.exists()) {
        instance.merge(file)
      }
    }
  }
}
