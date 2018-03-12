package profig

sealed trait ConfigType

object ConfigType {
  case object Auto extends ConfigType
  case object Json extends ConfigType
  case object Properties extends ConfigType
  case object Yaml extends ConfigType
  case object Hocon extends ConfigType
  case object XML extends ConfigType
}