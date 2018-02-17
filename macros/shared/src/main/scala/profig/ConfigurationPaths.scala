package profig

object ConfigurationPaths {
  var entries: List[ConfigurationPath] = List(
    ConfigurationPath("config.json", ConfigurationFileType.Json, LoadType.Merge),
    ConfigurationPath("config.conf", ConfigurationFileType.Auto, LoadType.Merge),
    ConfigurationPath("config.properties", ConfigurationFileType.Properties, LoadType.Merge),
    ConfigurationPath("config.yml", ConfigurationFileType.Yaml, LoadType.Merge),
    ConfigurationPath("config.yaml", ConfigurationFileType.Yaml, LoadType.Merge),

    ConfigurationPath("configuration.json", ConfigurationFileType.Json, LoadType.Merge),
    ConfigurationPath("configuration.conf", ConfigurationFileType.Auto, LoadType.Merge),
    ConfigurationPath("configuration.properties", ConfigurationFileType.Properties, LoadType.Merge),
    ConfigurationPath("configuration.yml", ConfigurationFileType.Yaml, LoadType.Merge),
    ConfigurationPath("configuration.yaml", ConfigurationFileType.Yaml, LoadType.Merge),

    ConfigurationPath("application.json", ConfigurationFileType.Json, LoadType.Merge),
    ConfigurationPath("application.conf", ConfigurationFileType.Auto, LoadType.Merge),
    ConfigurationPath("application.properties", ConfigurationFileType.Properties, LoadType.Merge),
    ConfigurationPath("application.yml", ConfigurationFileType.Yaml, LoadType.Merge),
    ConfigurationPath("application.yaml", ConfigurationFileType.Yaml, LoadType.Merge),

    ConfigurationPath("defaults.json", ConfigurationFileType.Json, LoadType.Defaults),
    ConfigurationPath("defaults.conf", ConfigurationFileType.Auto, LoadType.Defaults),
    ConfigurationPath("defaults.properties", ConfigurationFileType.Properties, LoadType.Defaults),
    ConfigurationPath("defaults.yml", ConfigurationFileType.Yaml, LoadType.Defaults),
    ConfigurationPath("defaults.yaml", ConfigurationFileType.Yaml, LoadType.Defaults)
  )
}