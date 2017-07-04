package profig

import java.io.File

object ProfigPlatform {
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
