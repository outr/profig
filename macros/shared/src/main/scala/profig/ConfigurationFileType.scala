package profig

sealed trait ConfigurationFileType

object ConfigurationFileType {
  case object Auto extends ConfigurationFileType
  case object Json extends ConfigurationFileType
  case object Properties extends ConfigurationFileType
  case object Yaml extends ConfigurationFileType
  case object Hocon extends ConfigurationFileType
  case object XML extends ConfigurationFileType
}