package profig

import java.io.File

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.language.experimental.macros
import scala.reflect.internal.util.ScalaClassLoader
import scala.reflect.macros.blackbox

object PlatformMacros {
  private[profig] val paths = List(
    "config.json",
    "config.conf",
    "configuration.json",
    "configuration.conf",
    "application.conf",
    "application.json"
  )
  private[profig] val defaults = List(
    "defaults.json",
    "defaults.conf"
  )

  def init(c: blackbox.Context)(): c.Expr[Unit] = {
    import c.universe._

    var loading = Set.empty[String]
    val expressions = ListBuffer.empty[Tree]

    val classLoader = ScalaClassLoader.fromURLs(c.classPath, PlatformMacros.getClass.getClassLoader)
    PlatformMacros.defaults.foreach { path =>
      Option(classLoader.getResource(path)).foreach { url =>
        loading += url.toString
        val source = Source.fromURL(url)
        try {
          val json = source.mkString
          expressions +=
            q"""
               val json = scala.scalajs.js.JSON.parse($json)
               io.circe.scalajs.convertJsToJson(json) match {
                 case Left(t) => throw t
                 case Right(j) => profig.Config.defaults(j)
               }
            """
        } finally {
          source.close()
        }
      }
    }
    PlatformMacros.defaults.foreach { path =>
      val file = new File(path)
      if (file.exists()) {
        val source = Source.fromFile(file)
        loading += file.getAbsolutePath
        try {
          val json = source.mkString
          expressions +=
            q"""
               val json = scala.scalajs.js.JSON.parse($json)
               io.circe.scalajs.convertJsToJson(json) match {
                 case Left(t) => throw t
                 case Right(j) => profig.Config.defaults(j)
               }
            """
        } finally {
          source.close()
        }
      }
    }
    PlatformMacros.paths.foreach { path =>
      Option(classLoader.getResource(path)).foreach { url =>
        val source = Source.fromURL(url)
        loading += url.toString
        try {
          val json = source.mkString
          expressions +=
            q"""
               val json = scala.scalajs.js.JSON.parse($json)
               io.circe.scalajs.convertJsToJson(json) match {
                 case Left(t) => throw t
                 case Right(j) => profig.Config.merge(j)
               }
            """
        } finally {
          source.close()
        }
      }
    }
    PlatformMacros.paths.foreach { path =>
      val file = new File(path)
      if (file.exists()) {
        val source = Source.fromFile(file)
        loading += file.getAbsolutePath
        try {
          val json = source.mkString
          expressions +=
            q"""
               val json = scala.scalajs.js.JSON.parse($json)
               io.circe.scalajs.convertJsToJson(json) match {
                 case Left(t) => throw t
                 case Right(j) => profig.Config.merge(j)
               }
            """
        } finally {
          source.close()
        }
      }
    }
    println(s"Loading runtime config with: ${loading.mkString(", ")} (${loading.size})")
    c.Expr[Unit](q"..$expressions")
  }
}