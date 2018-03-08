package profig

import io.circe.{Json, ParsingFailure}

object JsonParser {
  private var overrideParser: Option[String => Either[ParsingFailure, Json]] = None

  def compileTime(): Unit = {
    val parser = new io.circe.jawn.JawnParser
    overrideParser = Some(parser.parse _)
  }

  // TODO: can we remove compileTime() and make it automatic without optimization erroring?
//  private lazy val jawnParserClass = Class.forName("io.circe.jawn.JawnParser")
//  private lazy val jawnParserInstance = jawnParserClass.newInstance()
//  private lazy val parseMethod = jawnParserClass.getMethod("parse", classOf[String])

  /*def parse(jsonString: String): Either[ParsingFailure, Json] = overrideParser match {
    case Some(parser) => parser(jsonString)
    case None => io.circe.parser.parse(jsonString) match {
      case Left(exc) if exc.getMessage == "an implementation is missing" => {
        try {
//          val f = (string: String) => parseMethod.invoke(jawnParserInstance, string).asInstanceOf[Either[ParsingFailure, Json]]
          val parser = new io.circe.jawn.JawnParser
          val f = parser.parse _
          val result = f(jsonString)
          overrideParser = Some(f)
          result
        } catch {
          case _: Throwable => Left(exc)
        }
      }
      case result => result
    }
  }*/

  def parse(jsonString: String): Either[ParsingFailure, Json] = overrideParser match {
    case Some(parser) => parser(jsonString)
    case None => io.circe.parser.parse(jsonString) match {
      case Left(exc) if exc.getMessage() == "an implementation is missing" => throw new RuntimeException("If accessing from Macro, make sure to call JsonParser.compileTime() first", exc)
      case result => result
    }
  }
}