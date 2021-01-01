package profig

import io.circe.{Json, ParsingFailure}

object JsonParser {
  private var overrideParser: Option[String => Either[ParsingFailure, Json]] = None

  def parse(jsonString: String): Either[ParsingFailure, Json] = overrideParser match {
    case Some(parser) => parser(jsonString)
    case None => io.circe.parser.parse(jsonString) match {
      case Left(exc) if exc.getMessage() == "an implementation is missing" => {
        val parser = new io.circe.jawn.JawnParser
        val f = parser.parse _
        val result = f(jsonString)
        overrideParser = Some(f)
        result
      }
      case result => result
    }
  }
}
