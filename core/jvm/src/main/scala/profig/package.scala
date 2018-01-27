import java.io.File
import java.net.URL

import scala.io.Source

package object profig {
  implicit class JVMProfigPath(path: ProfigPath) {
    /**
      * Combines the `Source` instance into the current path. Source is detected as either JSON or Properties.
      *
      * @param source the source to load
      * @param defaults if true, will not overwrite existing values
      */
    def combine(source: Source, defaults: Boolean): Unit = {
      val string = try {
        source.mkString
      } finally {
        source.close()
      }
      path.combine(string, defaults)
    }

    /**
      * Merges (overwriting) existing values at this path from the `Source`.
      */
    def merge(source: Source): Unit = combine(source, defaults = false)

    /**
      * Merges (overwriting) existing values at this path from the `File`.
      */
    def merge(file: File): Unit = merge(Source.fromFile(file))

    /**
      * Merges (overwriting) existing values at this path from the `URL`.
      */
    def merge(url: URL): Unit = merge(Source.fromURL(url))

    /**
      * Loads defaults (does not overwrite) merging to current values in path from `Source`.
      */
    def defaults(source: Source): Unit = combine(source, defaults = true)

    /**
      * Loads defaults (does not overwrite) merging to current values in path from `File`.
      */
    def defaults(file: File): Unit = defaults(Source.fromFile(file))

    /**
      * Loads defaults (does not overwrite) merging to current values in path from `URL`.
      */
    def defaults(url: URL): Unit = defaults(Source.fromURL(url))
  }
}