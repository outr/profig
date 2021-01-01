package profig

import java.util.Properties

import scala.language.implicitConversions

trait SharedJSONConversions {
  implicit def properties2JSON(properties: Properties): Json = ProfigUtil.properties2Json(properties)
  implicit def args2JSON(args: Seq[String]): Json = ProfigUtil.args2Json(args)
}