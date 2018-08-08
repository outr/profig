package profig.input

case class InputData[T](arguments: List[InputArgument[_]], create: Vector[_] => T)
