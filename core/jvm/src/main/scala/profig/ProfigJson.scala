package profig

import fabric._

import java.util.Properties

trait ProfigJson {
  def apply(content: String): Json
}

object ProfigJson {
  var default: ProfigJson = Json

  private var map = Map.empty[String, ProfigJson]

  def types: Set[String] = map.keySet

  register(Json, "conf", "json", "config")
  register(Properties, "prop", "props", "properties")
  register(Hocon, "conf", "config", "hocon")
  register(XML, "xml")
  register(Yaml, "yaml", "yml")

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

  object Json extends ProfigJson {
    override def apply(content: String): Json = fabric.parse.JsonParser.parse(content)
  }

  object Properties extends ProfigJson {
    override def apply(content: String): Json = fabric.parse.Properties.parse(content)
  }

  object Hocon extends ProfigJson {
    override def apply(content: String): Json = fabric.parse.Hocon.parse(content)
  }

  object XML extends ProfigJson {
    override def apply(content: String): Json = fabric.parse.XML.parse(content)
  }

  object Yaml extends ProfigJson {
    override def apply(content: String): Json = fabric.parse.Yaml.parse(content)
  }
}