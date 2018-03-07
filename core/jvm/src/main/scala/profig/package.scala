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
    def combine(source: Source, `type`: ConfigurationFileType, defaults: Boolean): Unit = {
      val string = try {
        source.mkString
      } finally {
        source.close()
      }
      path.combine(string, `type`, defaults)
    }

    /**
      * Merges (overwriting) existing values at this path from the `Source`.
      */
    def merge(source: Source, `type`: ConfigurationFileType): Unit = combine(source, `type`, defaults = false)

    /**
      * Merges (overwriting) existing values at this path from the `File`.
      */
    def merge(file: File, `type`: ConfigurationFileType): Unit = merge(Source.fromFile(file), `type`)

    /**
      * Merges (overwriting) existing values at this path from the `URL`.
      */
    def merge(url: URL, `type`: ConfigurationFileType): Unit = merge(Source.fromURL(url), `type`)

    /**
      * Loads defaults (does not overwrite) merging to current values in path from `Source`.
      */
    def defaults(source: Source, `type`: ConfigurationFileType): Unit = combine(source, `type`, defaults = true)

    /**
      * Loads defaults (does not overwrite) merging to current values in path from `File`.
      */
    def defaults(file: File, `type`: ConfigurationFileType): Unit = defaults(Source.fromFile(file), `type`)

    /**
      * Loads defaults (does not overwrite) merging to current values in path from `URL`.
      */
    def defaults(url: URL, `type`: ConfigurationFileType): Unit = defaults(Source.fromURL(url), `type`)
  }
}