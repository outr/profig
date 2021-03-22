import java.util.Properties

import fabric._
import scala.language.implicitConversions

package object profig extends PlatformPickler {
  implicit def properties2JSON(properties: Properties): Value = ProfigUtil.properties2Json(properties)
  implicit def args2JSON(args: Seq[String]): Value = ProfigUtil.args2Json(args)
}
