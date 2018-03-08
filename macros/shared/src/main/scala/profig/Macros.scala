package profig

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object Macros {
  def fromJsonString[T](c: blackbox.Context)
                       (jsonString: c.Expr[String])
                       (implicit t: c.WeakTypeTag[T]): c.Expr[T] = {
    import c.universe._

    c.Expr[T](
      q"""
         profig.JsonParser.parse($jsonString) match {
           case Left(t) => throw t
           case Right(json) => profig.JsonUtil.fromJson[$t](json)
         }
       """)
  }

  def fromJson[T](c: blackbox.Context)
                 (json: c.Tree)
                 (implicit t: c.WeakTypeTag[T]): c.Expr[T] = {
    import c.universe._

    c.Expr[T](
      q"""
         import io.circe._
         import io.circe.generic.extras.Configuration
         import io.circe.generic.extras.auto._
         implicit val customConfig: Configuration = Configuration.default.withDefaults

         implicit val decoder = implicitly[Decoder[$t]]
         decoder.decodeJson($json) match {
           case Left(failure) => throw new RuntimeException("Failed to decode from " + $json, failure)
           case Right(value) => value
         }
       """)
  }

  def toJsonString[T](c: blackbox.Context)
                     (value: c.Expr[T])
                     (implicit t: c.WeakTypeTag[T]): c.Expr[String] = {
    import c.universe._

    c.Expr[String](q"profig.JsonUtil.toJson[$t]($value).pretty(io.circe.Printer.noSpaces)")
  }

  def toJson[T](c: blackbox.Context)
               (value: c.Expr[T])
               (implicit t: c.WeakTypeTag[T]): c.Tree = {
    import c.universe._

    q"""
       import io.circe._
       import io.circe.generic.extras.Configuration
       import io.circe.generic.extras.auto._
       implicit val customConfig: Configuration = Configuration.default.withDefaults

       val encoder = implicitly[Encoder[$t]]
       encoder($value)
     """
  }

  def as[T](c: blackbox.Context)(implicit t: c.WeakTypeTag[T]): c.Expr[T] = {
    import c.universe._

    val configPath = c.prefix.tree
    c.Expr[T](q"profig.JsonUtil.fromJson[$t]($configPath())")
  }

  def store[T](c: blackbox.Context)(value: c.Expr[T])(implicit t: c.WeakTypeTag[T]): c.Expr[Unit] = {
    import c.universe._

    val configPath = c.prefix.tree
    c.Expr[Unit](q"$configPath.merge(profig.JsonUtil.toJson[$t]($value))")
  }

  def loadFiles(c: blackbox.Context)(entries: c.Expr[ConfigurationPath]*): c.Tree = {
    import c.universe._

    implicit val cftLift: c.universe.Liftable[ConfigurationFileType] = Liftable[ConfigurationFileType] { cft =>
      q"_root_.profig.ConfigurationFileType.${TermName(cft.getClass.getSimpleName.replaceAllLiterally("$", ""))}"
    }

    val instance = c.prefix.tree
    if (profig.ProfigPlatform.isJS) {
      ConfigurationPath.yamlConversion = Some(ConfigurationPath.yamlString2Json)
      // TODO: support non-defaults in Scala.js
//      val entriesValue = entries match {
//        case Expr(Literal(Constant(value: Seq[ConfigurationPath]))) => value.toList
//      }

      val config = ConfigurationPath.toJsonStrings(ConfigurationPath.defaults).map {
        case (cp, json) => cp.load match {
          case LoadType.Defaults => q"$instance.defaults($json, ${cp.`type`})"
          case LoadType.Merge => q"$instance.merge($json, ${cp.`type`})"
        }
      }
      q"..$config"
    } else {
      q"""
         import profig._

         ConfigurationPath.toJsonStrings(List(..$entries)).foreach {
           case (cp, json) => cp.load match {
             case LoadType.Defaults => $instance.defaults(json, cp.`type`)
             case LoadType.Merge => $instance.merge(json, cp.`type`)
           }
         }
       """
    }
  }
}
