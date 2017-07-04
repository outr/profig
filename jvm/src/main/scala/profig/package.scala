import java.io.File
import java.net.URL

import scala.io.Source

package object profig {
  implicit class JVMConfigPath(path: ConfigPath) {
    def combine(source: Source, defaults: Boolean): Unit = {
      val string = try {
        source.mkString
      } finally {
        source.close()
      }
      path.combine(string, defaults)
    }

    def merge(source: Source): Unit = combine(source, defaults = false)
    def merge(file: File): Unit = merge(Source.fromFile(file))
    def merge(url: URL): Unit = merge(Source.fromURL(url))

    def defaults(source: Source): Unit = combine(source, defaults = true)
    def defaults(file: File): Unit = defaults(Source.fromFile(file))
    def defaults(url: URL): Unit = defaults(Source.fromURL(url))
  }
}