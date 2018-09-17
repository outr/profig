package profig

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor, Json, JsonObject}

object Conversions {
  implicit val booleanDecoder: Decoder[Boolean] = new Decoder[Boolean] {
    override def apply(c: HCursor): Result[Boolean] = {
      Right(if (c.value.isString) {
        c.value.asString.getOrElse("false").toBoolean
      } else if (c.value.isBoolean) {
        c.value.asBoolean.getOrElse(false)
      } else {
        c.value.asObject.getOrElse(JsonObject.empty)("value").flatMap(_.asString).getOrElse("false").toBoolean
      })
    }
  }
}
