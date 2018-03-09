package profig

import java.io.File
import java.net.URL
import java.util.Properties

import io.circe.{Json, yaml}

import scala.io.Source
import scala.collection.JavaConverters._

case class ConfigurationPath(path: String, `type`: ConfigurationFileType, load: LoadType)

object ConfigurationPath {
  var defaults: List[ConfigurationPath] = List(
    ConfigurationPath("config.json", ConfigurationFileType.Json, LoadType.Merge),
    ConfigurationPath("config.conf", ConfigurationFileType.Auto, LoadType.Merge),
    ConfigurationPath("config.properties", ConfigurationFileType.Properties, LoadType.Merge),
    ConfigurationPath("config.yml", ConfigurationFileType.Yaml, LoadType.Merge),
    ConfigurationPath("config.yaml", ConfigurationFileType.Yaml, LoadType.Merge),
    ConfigurationPath("config.hocon", ConfigurationFileType.Hocon, LoadType.Merge),
    ConfigurationPath("config.xml", ConfigurationFileType.XML, LoadType.Merge),

    ConfigurationPath("configuration.json", ConfigurationFileType.Json, LoadType.Merge),
    ConfigurationPath("configuration.conf", ConfigurationFileType.Auto, LoadType.Merge),
    ConfigurationPath("configuration.properties", ConfigurationFileType.Properties, LoadType.Merge),
    ConfigurationPath("configuration.yml", ConfigurationFileType.Yaml, LoadType.Merge),
    ConfigurationPath("configuration.yaml", ConfigurationFileType.Yaml, LoadType.Merge),
    ConfigurationPath("configuration.hocon", ConfigurationFileType.Hocon, LoadType.Merge),
    ConfigurationPath("configuration.xml", ConfigurationFileType.XML, LoadType.Merge),

    ConfigurationPath("app.json", ConfigurationFileType.Json, LoadType.Merge),
    ConfigurationPath("app.conf", ConfigurationFileType.Auto, LoadType.Merge),
    ConfigurationPath("app.properties", ConfigurationFileType.Properties, LoadType.Merge),
    ConfigurationPath("app.yml", ConfigurationFileType.Yaml, LoadType.Merge),
    ConfigurationPath("app.yaml", ConfigurationFileType.Yaml, LoadType.Merge),
    ConfigurationPath("app.hocon", ConfigurationFileType.Hocon, LoadType.Merge),
    ConfigurationPath("app.xml", ConfigurationFileType.XML, LoadType.Merge),

    ConfigurationPath("application.json", ConfigurationFileType.Json, LoadType.Merge),
    ConfigurationPath("application.conf", ConfigurationFileType.Auto, LoadType.Merge),
    ConfigurationPath("application.properties", ConfigurationFileType.Properties, LoadType.Merge),
    ConfigurationPath("application.yml", ConfigurationFileType.Yaml, LoadType.Merge),
    ConfigurationPath("application.yaml", ConfigurationFileType.Yaml, LoadType.Merge),
    ConfigurationPath("application.hocon", ConfigurationFileType.Hocon, LoadType.Merge),
    ConfigurationPath("application.xml", ConfigurationFileType.XML, LoadType.Merge),

    ConfigurationPath("defaults.json", ConfigurationFileType.Json, LoadType.Defaults),
    ConfigurationPath("defaults.conf", ConfigurationFileType.Auto, LoadType.Defaults),
    ConfigurationPath("defaults.properties", ConfigurationFileType.Properties, LoadType.Defaults),
    ConfigurationPath("defaults.yml", ConfigurationFileType.Yaml, LoadType.Defaults),
    ConfigurationPath("defaults.yaml", ConfigurationFileType.Yaml, LoadType.Defaults),
    ConfigurationPath("defaults.hocon", ConfigurationFileType.Hocon, LoadType.Defaults),
    ConfigurationPath("defaults.xml", ConfigurationFileType.XML, LoadType.Defaults)
  )

  def toStrings(entries: List[ConfigurationPath] = defaults): List[(ConfigurationPath, String)] = if (entries.isEmpty) {
    Nil
  } else {
    val entry = entries.head
    val classLoaderStrings = getClass.getClassLoader.getResources(entry.path).asScala.toList.map(fromURL)
    val fileStrings = fromFile(new File(entry.path)).toList
    val strings = classLoaderStrings.flatten ::: fileStrings
    val list = strings.map(entry -> _)
    list ::: toStrings(entries.tail)
  }

  def toJsonStrings(entries: List[ConfigurationPath] = defaults): List[(ConfigurationPath, String)] = {
    toStrings(entries).map {
      case (cp, string) => {
        if (cp.`type` == ConfigurationFileType.Json) {
          cp -> string
        } else {
          cp.copy(`type` = ConfigurationFileType.Json) -> toJson(string, cp.`type`).spaces2
        }
      }
    }
  }

  private def fromFile(file: File): Option[String] = if (file.isFile) {
    val source = Source.fromFile(file)
    try {
      Some(source.mkString)
    } finally {
      source.close()
    }
  } else {
    None
  }

  private def fromURL(url: URL): Option[String] = if (Option(url).nonEmpty) {
    val source = Source.fromURL(url)
    try {
      Some(source.mkString)
    } finally {
      source.close()
    }
  } else {
    None
  }

  def jsonString2Json(string: String): Json = JsonParser.parse(string) match {
    case Left(failure) => throw new RuntimeException(s"Unable to parse $string to JSON.", failure)    // TODO: deal with implementation missing
    case Right(value) => value
  }

  private val EqualsProperty = """(.+)=(.+)""".r
  private val ColonProperty = """(.+)[:](.+)""".r

  def propertiesString2Json(string: String): Json = {
    val properties = new Properties
    var continuing: Option[(String, String)] = None
    string.split('\n').foreach {
      case line if line.startsWith("#") || line.startsWith("!") =>   // Ignore
      case line => {
        continuing match {
          case Some((key, value)) => if (line.endsWith("\\")) {       // Continued key/value pair
            continuing = Some(key -> s"$value\n${line.substring(0, line.length - 1).trim}")
          } else {
            properties.put(key, s"$value\n${line.trim}")
          }
          case None => line match {                                  // New key/value pair
            case EqualsProperty(key, value) => if (value.endsWith("\\")) {
              continuing = Some(key.trim, value.substring(0, value.length - 1).trim)
            } else {
              properties.put(key.trim, value.trim)
            }
            case ColonProperty(key, value) => if (value.endsWith("\\")) {
              continuing = Some(key.trim, value.substring(0, value.length - 1).trim)
            } else {
              properties.put(key.trim, value.trim)
            }
          }
        }
      }
    }
    ConfigUtil.properties2Json(properties)
  }

  var yamlConversion: Option[String => Json] = ProfigPlatform.yamlConversion

  def yamlString2Json(string: String): Json = yaml.parser.parse(string) match {
    case Left(failure) => throw new RuntimeException(s"Unable to parse $string (YAML) to JSON.", failure)
    case Right(value) => value
  }

  def hoconString2Json(string: String): Json = {
    import eu.unicredit.shocon._

    def toJson(value: Config.Value): Json = value match {
      case Config.Array(elements) => Json.arr(elements.map(toJson): _*)
      case Config.Object(fields) => Json.obj(fields.map {
        case (k, v) => k -> toJson(v)
      }.toSeq: _*)
      case Config.NumberLiteral(v) => Json.fromDouble(v.toDouble).getOrElse(throw new RuntimeException(s"Unable to convert $v to JsonNumber"))
      case Config.StringLiteral(v) => Json.fromString(v)
      case Config.BooleanLiteral(v) => Json.fromBoolean(v)
      case Config.NullLiteral => Json.Null
      case _ => throw new UnsupportedOperationException(s"Unsupported HOCON value: $value")
    }

    toJson(Config(string))
  }

  def xmlString2Json(string: String): Json = {
    import scala.xml._

    def toJson(node: Node): Option[Json] = node match {
      case elem: Elem => {
        val attributes: List[(String, Json)] = elem.attributes.map(md => toJson(md.value.head).map(md.key -> _)).toList.flatten
        val children = elem.child.toList.collect {
          case child: Elem => toJson(child).map(child.label -> _)
        }.flatten
        val text = elem.text.trim
        if (attributes.isEmpty && children.isEmpty) {
          if (text.isEmpty) {
            None
          } else {
            Some(Json.fromString(text))
          }
        } else {
          Some(Json.obj(attributes ::: children: _*))
        }
      }
      case _ => None
    }

    val root = XML.loadString(string)
    Json.obj(root.label -> toJson(root).getOrElse(Json.Null))
  }

  def toJson(string: String, `type`: ConfigurationFileType): Json = `type` match {
    case ConfigurationFileType.Json => jsonString2Json(string)
    case ConfigurationFileType.Properties => propertiesString2Json(string)
    case ConfigurationFileType.Yaml => yamlConversion.map(c => c(string)).getOrElse(throw new RuntimeException(s"YAML conversion not supported."))
    case ConfigurationFileType.Hocon => hoconString2Json(string)
    case ConfigurationFileType.XML => xmlString2Json(string)
    case ConfigurationFileType.Auto => if (string.trim.startsWith("{")) {
      jsonString2Json(string)
    } else {
      propertiesString2Json(string)
    }
  }
}