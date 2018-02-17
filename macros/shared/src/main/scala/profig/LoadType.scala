package profig

sealed trait LoadType

object LoadType {
  case object Merge extends LoadType
  case object Defaults extends LoadType
}