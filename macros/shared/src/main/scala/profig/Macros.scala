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

    val companion = symbolOf[T].companion
    val imprt = if (companion.isStatic) {
      q"import $companion._"
    } else {
      q""
    }
    c.Expr[T](
      q"""
         import io.circe._
         import io.circe.generic.extras
         import io.circe.generic.extras.auto._
         $imprt
         implicit val customConfig: extras.Configuration = extras.Configuration.default.withDefaults

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

    val companion = symbolOf[T].companion
    val imprt = if (companion.isStatic) {
      q"import $companion._"
    } else {
      q""
    }
    q"""
       import io.circe._
       import io.circe.generic.extras
       import io.circe.generic.extras.auto._
       $imprt
       implicit val customConfig: extras.Configuration = extras.Configuration.default.withDefaults

       val encoder = implicitly[Encoder[$t]]
       encoder($value)
     """
  }

  def as[T](c: blackbox.Context)(implicit t: c.WeakTypeTag[T]): c.Expr[T] = {
    import c.universe._

    val configPath = c.prefix.tree
    if (t.tpe <:< typeOf[Option[_]]) {
      c.warning(c.enclosingPosition, "Use 'opt' instead of 'as' for cleaner code with Option")
    }
    c.Expr[T](q"profig.JsonUtil.fromJson[$t]($configPath())")
  }

  def asWithDefault[T](c: blackbox.Context)(default: c.Tree)(implicit t: c.WeakTypeTag[T]): c.Expr[T] = {
    import c.universe._

    val configPath = c.prefix.tree
    c.Expr[T](q"$configPath.get().map(json => profig.JsonUtil.fromJson[$t](json)).getOrElse($default)")
  }

  def opt[T](c: blackbox.Context)(implicit t: c.WeakTypeTag[T]): c.Expr[Option[T]] = {
    import c.universe._

    val configPath = c.prefix.tree
    c.Expr[Option[T]](q"$configPath.get().map(json => profig.JsonUtil.fromJson[$t](json))")
  }

  def store[T](c: blackbox.Context)(value: c.Expr[T])(implicit t: c.WeakTypeTag[T]): c.Expr[Unit] = {
    import c.universe._

    val configPath = c.prefix.tree
    c.Expr[Unit](q"$configPath.merge(profig.JsonUtil.toJson[$t]($value))")
  }

  def loadDefaults(c: blackbox.Context)(): c.Tree = {
    import c.universe._

    load(c)(reify(ProfigLookupPath.defaults: _*))
  }

  def load(c: blackbox.Context)(entries: c.Expr[ProfigLookupPath]*): c.Tree = {
    import c.universe._

    implicit val cftLift: c.universe.Liftable[FileType] = Liftable[FileType] { cft =>
      q"_root_.profig.FileType.${TermName(cft.getClass.getSimpleName.replaceAllLiterally("$", ""))}"
    }

    val instance = c.prefix.tree
    if (profig.ProfigPlatform.isJS) {
      ProfigLookupPath.yamlConversion = Some(ProfigLookupPath.yamlString2Json)

      val config = ProfigLookupPath.toJsonStrings().map {
        case (cp, json) => cp.load match {
          case LoadType.Defaults => q"$instance.defaults($json, ${cp.`type`})"
          case LoadType.Merge => q"$instance.merge($json, ${cp.`type`})"
        }
      }
      q"..$config"
    } else {
      q"""
         import profig._

         ProfigLookupPath.toJsonStrings(List(..$entries)).foreach {
           case (cp, json) => cp.load match {
             case LoadType.Defaults => $instance.defaults(json, cp.`type`)
             case LoadType.Merge => $instance.merge(json, cp.`type`)
           }
         }
       """
    }
  }
}
