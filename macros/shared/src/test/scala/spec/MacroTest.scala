package spec

import profig.JsonParser

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object MacroTest {
  def format(jsonString: String): String = macro compileTimeFormat

  def compileTimeFormat(c: blackbox.Context)(jsonString: c.Expr[String]): c.Expr[String] = {
    import c.universe._

    val jsonStringValue = jsonString match {
      case Expr(Literal(Constant(value: String))) => value
    }

    val json = JsonParser.parse(jsonStringValue) match {
      case Left(failure) => throw new RuntimeException(s"Unable to parse $jsonStringValue to JSON.", failure)
      case Right(value) => value
    }
    c.Expr[String](q"${json.spaces2}")
  }
}