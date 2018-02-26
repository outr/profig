package profig

/**
  * Platform-specific initialization for JVM
  */
object ProfigPlatform {
  def loadFiles(instance: Profig, entries: List[ConfigurationPath]): Unit = {
    ConfigurationPath.toJsonStrings(entries).foreach {
      case (cp, json) => cp.load match {
        case LoadType.Defaults => instance.defaults(json)
        case LoadType.Merge => instance.merge(json)
      }
    }
  }
}