package profig.input

case class InputArgument[A](name: String, convert: String => Option[A], default: Option[A], `type`: String) {
  def convertOrDefault(value: String): Option[A] = convert(value).orElse(default)
}
