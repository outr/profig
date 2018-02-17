package profig

import java.io.File

import scala.collection.mutable.ListBuffer
import scala.io.Source
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object PlatformMacros {
  def init(c: blackbox.Context)(instance: c.Tree): c.Expr[Unit] = {
    import c.universe._

    var loading = Set.empty[String]
    val expressions = ListBuffer.empty[Tree]

    def addExpression(jsonString: String, defaults: Boolean): Unit = {
      val combine = if (defaults) q"$instance.defaults(j)" else q"$instance.merge(j)"
      if (profig.Macros.inlined.get()) {
        expressions +=
          q"""
             io.circe.jawn.parse($jsonString) match {
               case Left(t) => throw t
               case Right(j) => $combine
             }
           """
      } else {
        expressions +=
          q"""
             val json = scala.scalajs.js.JSON.parse($jsonString)
             io.circe.scalajs.convertJsToJson(json) match {
               case Left(t) => throw t
               case Right(j) => $combine
             }
           """
      }
    }
    val classLoader = PlatformMacros.getClass.getClassLoader
    defaults.foreach { path =>
      Option(classLoader.getResource(path)).foreach { url =>
        loading += url.toString
        val source = Source.fromURL(url)
        try {
          val jsonString = source.mkString
          addExpression(jsonString, defaults = true)
        } finally {
          source.close()
        }
      }
    }
    defaults.foreach { path =>
      val file = new File(path)
      if (file.exists()) {
        val source = Source.fromFile(file)
        loading += file.getAbsolutePath
        try {
          val jsonString = source.mkString
          addExpression(jsonString, defaults = true)
        } finally {
          source.close()
        }
      }
    }
    paths.foreach { path =>
      Option(classLoader.getResource(path)).foreach { url =>
        val source = Source.fromURL(url)
        loading += url.toString
        try {
          val jsonString = source.mkString
          addExpression(jsonString, defaults = false)
        } finally {
          source.close()
        }
      }
    }
    paths.foreach { path =>
      val file = new File(path)
      if (file.exists()) {
        val source = Source.fromFile(file)
        loading += file.getAbsolutePath
        try {
          val jsonString = source.mkString
          addExpression(jsonString, defaults = false)
        } finally {
          source.close()
        }
      }
    }
    c.Expr[Unit](q"""
      ..$expressions
      println("Loaded configuration with: " + List(..$loading).mkString(", ") + "...")
    """)
  }
}