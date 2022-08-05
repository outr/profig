import java.util.Properties

import fabric._
import scala.language.implicitConversions

package object profig extends PlatformPickler {
  implicit def properties2JSON(properties: Properties): Json = ProfigUtil.properties2Json(properties)
  implicit def args2JSON(args: Seq[String]): Json = ProfigUtil.args2Json(args)
}
