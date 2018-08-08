package profig.input

import scala.concurrent.{ExecutionContext, Future}
import scala.language.experimental.macros
import scala.language.implicitConversions

trait ProfigInput {
  def read[T](data: InputData[T])(implicit executionContext: ExecutionContext): Future[Option[T]] = {
    readStrings[T](data).map { args =>
      try {
        val vector = args.map(_.getOrElse("")).zip(data.arguments).map {
          case (value, arg) => {
            arg.convertOrDefault(value).getOrElse(throw new RuntimeException(s"Unable to parse $value for $arg"))
          }
        }.toVector
        Option(data.create(vector))
      } catch {
        case _: Throwable => None
      }
    }
  }

  protected def readStrings[T](data: InputData[T]): Future[Seq[Option[String]]]
}

object ProfigInput {
  implicit def string2String(s: String): Option[String] = Option(s)
  implicit def string2Int(s: String): Option[Int] = try {
    Some(s.toInt)
  } catch {
    case _: NumberFormatException => None
  }

  def createInputData[T]: InputData[T] = macro InputMacros.createInputData[T]
}