package profig

import java.io.File

object ProfigJVM {
  def init(): Unit = {
    PlatformMacros.defaults.foreach { path =>
      Option(getClass.getClassLoader.getResource(path)).foreach { url =>
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
      Option(getClass.getClassLoader.getResource(path)).foreach { url =>
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
