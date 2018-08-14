package profig.input

import scala.collection.mutable.ListBuffer
import scala.language.experimental.macros
import scala.reflect.macros.blackbox

object InputMacros {
  def createInputData[T](c: blackbox.Context)(implicit t: c.WeakTypeTag[T]): c.Expr[InputData[T]] = {
    import c.universe._

    val classSymbol = t.tpe.typeSymbol
    val moduleSymbol = classSymbol.companion
    val apply = moduleSymbol.typeSignature.decl(TermName("apply")).asMethod
    val params = apply.paramLists.head.map(_.asTerm).zipWithIndex
    val args = ListBuffer.empty[c.Tree]
    val conversion = ListBuffer.empty[c.Tree]
    params.map {
      case (param, index) => {
        val name = param.name.decodedName.toString
        val result = param.typeSignature
        val default = if (param.isParamWithDefault) {
          val getterName = TermName("apply$default$" + (index + 1))
          q"Some($moduleSymbol.$getterName)"
        } else {
          q"None"
        }
        args += q"profig.input.InputArgument[$result]($name, implicitly[String => Option[$result]], $default, ${result.toString})"
        conversion += q"v($index).asInstanceOf[$result]"
      }
    }

    val inputData =
      q"""
         import profig.input.ProfigInput._

         profig.input.InputData[$t](List(..$args), (v: Vector[_]) => $moduleSymbol(..$conversion), ${classSymbol.toString})
       """
    c.Expr[InputData[T]](inputData)
  }
}