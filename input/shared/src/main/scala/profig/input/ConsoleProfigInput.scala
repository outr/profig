package profig.input

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn

object ConsoleProfigInput extends ProfigInput {
  override protected def readStrings[T](data: InputData[T]): Future[Seq[Option[String]]] = Future {
    data.arguments.map { arg =>
      Option(prompt(arg))
    }
  }

  private def prompt(arg: InputArgument[_]): String = {
    StdIn.readLine(s"  ${arg.name.capitalize} (${arg.`type`}) [${arg.default.map(_.toString).getOrElse("")}]: ")
  }
}