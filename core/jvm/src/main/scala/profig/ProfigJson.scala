package profig

import java.util.Properties

import io.circe.Json

trait ProfigJson {
  def apply(content: String): Json
}

object ProfigJson {
  var default: ProfigJson = Circe

  private var map = Map.empty[String, ProfigJson]

  def types: Set[String] = map.keySet

  register(Circe, "conf", "json", "config")
  register(Properties, "prop", "props", "properties")

  def apply(content: String, `type`: Option[String]): Json = {
    val pj = `type`.flatMap(map.get).getOrElse(default)
    pj(content)
  }

  def register(pj: ProfigJson, types: String*): Unit = synchronized {
    types.foreach { t =>
      FileNameMatcher.DefaultExtensions += t
      map += t -> pj
    }
  }

  object Circe extends ProfigJson {
    override def apply(content: String): Json = io.circe.parser.parse(content) match {
      case Left(pf) => throw new RuntimeException(s"Failed to parse JSON from: $content", pf)
      case Right(json) => json
    }
  }

  object Properties extends ProfigJson {
    private val EqualsProperty = """(.+?)=(.+)""".r
    private val ColonProperty = """(.+?)[:](.+)""".r

    override def apply(content: String): Json = {
      val properties = new Properties
      var continuing: Option[(String, String)] = None
      content.split('\n').filter(_.nonEmpty).foreach {
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
  }
}