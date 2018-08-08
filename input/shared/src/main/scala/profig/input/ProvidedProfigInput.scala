package profig.input

import scala.concurrent.Future

class ProvidedProfigInput(values: List[String]) extends ProfigInput {
  override protected def readStrings[T](data: InputData[T]): Future[Seq[Option[String]]] = {
    Future.successful(values.map(Option.apply))
  }
}