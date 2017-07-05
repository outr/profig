package profig

import java.io.File

/**
  * Platform-specific initialization for JVM
  */
object ProfigPlatform {
  /**
    * Called upon initialization of Config at first use. Attempts to load the following from the classloader and then
    * from the local filesystem:
    *   - config.json
    *   - config.conf
    *   - configuration.json
    *   - configuration.conf
    *   - application.conf
    *   - application.json
    * The order matters, so later configuration files will overwrite former. Similarly, files will overwrite
    * configuration in the classloader.
    */
  def init(): Unit = {
    val paths = List(
      "config.json",
      "config.conf",
      "configuration.json",
      "configuration.conf",
      "application.conf",
      "application.json"
    )
    paths.foreach { path =>
      Option(getClass.getClassLoader.getResource(path)).foreach { url =>
        Config.merge(url)
      }
    }
    paths.foreach { path =>
      val file = new File(path)
      if (file.exists()) {
        Config.merge(file)
      }
    }
  }
}
