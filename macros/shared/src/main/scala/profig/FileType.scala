package profig

sealed trait FileType

object FileType {
  case object Auto extends FileType
  case object Json extends FileType
  case object Properties extends FileType
  case object Yaml extends FileType
  case object Hocon extends FileType
  case object XML extends FileType
}