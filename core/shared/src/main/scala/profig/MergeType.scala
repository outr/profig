package profig

sealed trait MergeType

object MergeType {
  case object Overwrite extends MergeType
  case object Add extends MergeType
}