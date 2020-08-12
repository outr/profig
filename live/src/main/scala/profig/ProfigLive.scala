package profig

import java.nio.file.{FileSystems, Files, Path, Paths, WatchEvent}
import java.nio.file.StandardOpenOption._
import java.nio.file.StandardWatchEventKinds._

import io.circe.{Json, Printer}
import reactify.{Channel, Val, Var}

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

// TODO: This needs to be thought out a lot more, perhaps in 3.1
object ProfigLive {
  def immutable[T](default: T,
                   config: LiveConfig): Val[T] = macro immutableMacro[T]

  def mutable[T](default: T,
                 config: LiveConfig): Var[T] = macro mutableMacro[T]

  def mutable[T](default: T,
                 config: LiveConfig,
                 toJSON: T => Json,
                 fromJSON: Json => T): Var[T] = {
    var lastModified = 0L
    val v = Var[T](default)
    var modifying = false
    if (config.readFileChanges) {
      val watcher = FileSystems.getDefault.newWatchService()
      val directory = config.file.toFile.getCanonicalFile.getParentFile.toPath
      if (!Files.exists(directory)) {
        Files.createDirectory(directory)
      }
      directory.register(watcher, ENTRY_MODIFY)
      val thread = new Thread {
        setDaemon(true)

        override def run(): Unit = while(true) {
          watcher.take()
          v.synchronized {
            if (!modifying) {
              modifying = true
              try {
                readFromFile()
              } finally {
                modifying = false
              }
            }
          }
        }
      }
      thread.start()
    }
    def readFromFile(): Unit = {
      val modified = if (Files.exists(config.file)) {
        Files.getLastModifiedTime(config.file).toMillis
      } else {
        0L
      }
      if (modified != lastModified) {
        lastModified = modified
        try {
          val jsonString = new String(Files.readAllBytes(config.file), "UTF-8")
          val json = ProfigJson.Circe(jsonString)
          v @= fromJSON(json)
        } catch {
          case t: Throwable => config.errorHandler(t)
        }
      }
    }
    readFromFile()
    if (config.writeValueChanges) {
      v.attach { value =>
        v.synchronized {
          if (!modifying) {
            modifying = true
            try {
              val json = toJSON(value)
              Files.write(config.file, json.printWith(Printer.spaces2).getBytes("UTF-8"), CREATE, WRITE, TRUNCATE_EXISTING)
            } finally {
              modifying = false
            }
          }
        }
      }
    }
    v
  }

  def immutableMacro[T](c: blackbox.Context)
                       (default: c.Expr[T], config: c.Expr[LiveConfig])
                       (implicit t: c.WeakTypeTag[T]): c.Expr[Val[T]] = {
    import c.universe._

    c.Expr[Val[T]](
      q"""
          import _root_.profig._
          import _root_.io.circe.Json

          val toJSON: $t => Json = (t: $t) => JsonUtil.toJson[$t](t)
          val fromJSON: Json => $t = (json: Json) => JsonUtil.fromJson[$t](json)
          ProfigLive.mutable[$t]($default, $config, toJSON, fromJSON)
         """
    )
  }

  def mutableMacro[T](c: blackbox.Context)
                     (default: c.Expr[T], config: c.Expr[LiveConfig])
                     (implicit t: c.WeakTypeTag[T]): c.Expr[Var[T]] = {
    import c.universe._

    c.Expr[Var[T]](
      q"""
          import _root_.profig._
          import _root_.io.circe.Json

          val toJSON: $t => Json = (t: $t) => JsonUtil.toJson[$t](t)
          val fromJSON: Json => $t = (json: Json) => JsonUtil.fromJson[$t](json)
          ProfigLive.mutable[$t]($default, $config, toJSON, fromJSON)
         """
    )
  }
}