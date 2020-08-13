package profig

trait FileNameMatcher {
  def matches(prefix: String, extension: String): Option[MergeType]
}

object FileNameMatcher {
  var OverwritePrefixes: Set[String] = Set("config", "configuration", "app", "application")
  var AddPrefixes: Set[String] = Set("default", "defaults")
  var DefaultExtensions: Set[String] = Set("json", "properties", "conf", "config")

  case object Default extends FileNameMatcher {
    override def matches(prefix: String, extension: String): Option[MergeType] = {
      if (DefaultExtensions.contains(extension)) {
        if (OverwritePrefixes.contains(prefix)) {
          Some(MergeType.Overwrite)
        } else if (AddPrefixes.contains(prefix)) {
          Some(MergeType.Add)
        } else {
          None
        }
      } else {
        None
      }
    }
  }
}