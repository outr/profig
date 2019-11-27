package profig

import java.io.File
import java.net.URL
import java.util.Properties

import io.circe.{Json, yaml}

import scala.io.Source
import scala.collection.JavaConverters._

case class ProfigLookupPath(path: String, `type`: FileType, load: LoadType)

object ProfigLookupPath {
  var extensions: Map[String, FileType] = Map(
    "json" -> FileType.Json,
    "properties" -> FileType.Properties,
    "yml" -> FileType.Yaml,
    "yaml" -> FileType.Yaml,
//    "hocon" -> FileType.Hocon,
    "xml" -> FileType.XML,
    "conf" -> FileType.Auto,
    "config" -> FileType.Auto
  )

  var mergePaths: List[String] = List("config", "configuration", "app", "application")
  var defaultPaths: List[String] = List("defaults")

  def paths(mergePaths: List[String] = mergePaths,
            defaultPaths: List[String] = defaultPaths,
            extensions: Map[String, FileType] = extensions): List[ProfigLookupPath] = {
    val merge = mergePaths.flatMap(p => extensions.map {
      case (ext, configType) => ProfigLookupPath(s"$p.$ext", configType, LoadType.Merge)
    })
    val defaults = defaultPaths.flatMap(p => extensions.map {
      case (ext, configType) => ProfigLookupPath(s"$p.$ext", configType, LoadType.Defaults)
    })
    merge ::: defaults
  }

  def defaults: List[ProfigLookupPath] = paths()

  def toStrings(entries: List[ProfigLookupPath] = defaults): List[(ProfigLookupPath, String)] = if (entries.isEmpty) {
    Nil
  } else {
    val entry = entries.head
    val classLoaderStrings = getClass.getClassLoader.getResources(entry.path).asScala.toList.map(fromURL)
    val fileStrings = fromFile(new File(entry.path)).toList
    val strings = classLoaderStrings.flatten ::: fileStrings
    val list = strings.map(entry -> _)
    list ::: toStrings(entries.tail)
  }

  def toJsonStrings(entries: List[ProfigLookupPath] = defaults): List[(ProfigLookupPath, String)] = {
    toStrings(entries).map {
      case (cp, string) => {
        if (cp.`type` == FileType.Json) {
          cp -> string
        } else {
          cp.copy(`type` = FileType.Json) -> toJson(string, cp.`type`).spaces2
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

  private val EqualsProperty = """(.+?)=(.+)""".r
  private val ColonProperty = """(.+?)[:](.+)""".r

  def propertiesString2Json(string: String): Json = {
    val properties = new Properties
    var continuing: Option[(String, String)] = None
    string.split('\n').filter(_.nonEmpty).foreach {
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
            case _ => // Not supported
          }
        }
      }
    }
    ProfigUtil.properties2Json(properties)
  }

  var yamlConversion: Option[String => Json] = ProfigPlatform.yamlConversion

  def yamlString2Json(string: String): Json = yaml.parser.parse(string) match {
    case Left(failure) => throw new RuntimeException(s"Unable to parse $string (YAML) to JSON.", failure)
    case Right(value) => value
  }

  /*def hoconString2Json(string: String): Json = {
    import org.akkajs.shocon._

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
  }*/

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

  def toJson(string: String, `type`: FileType): Json = `type` match {
    case FileType.Json => jsonString2Json(string)
    case FileType.Properties => propertiesString2Json(string)
    case FileType.Yaml => yamlConversion.map(c => c(string)).getOrElse(throw new RuntimeException(s"YAML conversion not supported."))
//    case FileType.Hocon => hoconString2Json(string)
    case FileType.XML => xmlString2Json(string)
    case FileType.Auto => if (string.trim.startsWith("{")) {
      jsonString2Json(string)
    } else {
      propertiesString2Json(string)
    }
  }
}