import java.io.File
import java.net.URL
import java.util.Properties

import com.typesafe.config.ConfigFactory
import io.circe.Json

import scala.io.Source
import scala.language.implicitConversions

package object profig extends SharedJSONConversions {
  implicit def file2JSON(file: File): Json = source2Json(Source.fromFile(file), Some(file.getName))
  implicit def url2JSON(url: URL): Json = source2Json(Source.fromURL(url), Some(url.getFile))
  implicit def source2JSON(source: Source): Json = source2Json(source, None)

  def source2Json(source: Source, fileName: Option[String]): Json = {
    val extension = fileName.flatMap { fn =>
      val index = fn.lastIndexOf('.')
      if (index != -1) {
        Some(fn.substring(index + 1).toLowerCase)
      } else {
        None
      }
    }
    val s = source2String(source)
    extension match {
      case Some("json") => string2Json(s)
      case Some("prop") => propertiesString2Json(s)
      case Some("properties") => propertiesString2Json(s)
      case Some("xml") => xmlString2Json(s)
      case Some("yml") => yamlString2Json(s)
      case Some("yaml") => yamlString2Json(s)
      case _ => hocon2JSON(s)
    }
  }

  private def hocon2JSON(s: String): Json = {
    val conf = ConfigFactory.parseString(s).resolve()
    val jsonString = conf.root().render()
    string2Json(jsonString)
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

  private def yamlString2Json(string: String): Json = io.circe.yaml.parser.parse(string) match {
    case Left(failure) => throw new RuntimeException(s"Unable to parse $string (YAML) to JSON.", failure)
    case Right(value) => value
  }

  private def source2String(source: Source): String = try {
    source.mkString("\n")
  } finally {
    source.close()
  }

  private def string2Json(s: String): Json = io.circe.parser.parse(s) match {
    case Left(pf) => throw pf
    case Right(json) => json
  }
}