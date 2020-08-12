package profig

import java.nio.file.{Path, Paths}

case class LiveConfig(file: Path = Paths.get("config.json"),
                      readFileChanges: Boolean = true,
                      writeValueChanges: Boolean = true,
                      errorHandler: Throwable => Unit = t => sys.error(s"Error in LiveConfig: ${t.getMessage}"))