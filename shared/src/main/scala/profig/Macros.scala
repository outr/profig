package profig

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object Macros {
  def as[T](c: blackbox.Context)(implicit t: c.WeakTypeTag[T]): c.Expr[T] = {
    import c.universe._

    val configPath = c.prefix.tree
    c.Expr[T](
      q"""
         import io.circe._
         import io.circe.generic.extras.Configuration
         import io.circe.generic.extras.auto._
         implicit val customConfig: Configuration = Configuration.default.withSnakeCaseKeys.withDefaults

         val json = $configPath()
         implicit val decoder = implicitly[Decoder[$t]]
         decoder.decodeJson(json) match {
           case Left(failure) => throw new RuntimeException(s"Failed to decode from $$json ($${json.getClass})", failure)
           case Right(value) => value
         }
       """)
  }

  def store[T](c: blackbox.Context)(value: c.Expr[T])(implicit t: c.WeakTypeTag[T]): c.Expr[Unit] = {
    import c.universe._

    val configPath = c.prefix.tree
    c.Expr[Unit](
      q"""
         import io.circe._
         import io.circe.generic.extras.Configuration
         import io.circe.generic.extras.auto._
         implicit val customConfig: Configuration = Configuration.default.withSnakeCaseKeys.withDefaults

         val encoder = implicitly[Encoder[$t]]
         val json = encoder($value)
         $configPath.merge(json)
       """)
  }

  def init(c: blackbox.Context)(args: c.Expr[Seq[String]]): c.Expr[Unit] = {
    import c.universe._

    c.Expr[Unit](
      q"""
         profig.ProfigPlatform.init()
         profig.Config.merge($args)
       """)
  }

  def start(c: blackbox.Context)(args: c.Expr[Seq[String]]): c.Expr[Unit] = {
    import c.universe._

    val mainClass = c.prefix.tree
    c.Expr[Unit](
      q"""
         profig.ProfigPlatform.init()
         profig.Config.merge($args)
         $mainClass.run()
       """)
  }
}
