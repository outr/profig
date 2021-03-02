package profig

import hierarchical._

import java.util.Properties

trait ProfigJson {
  def apply(content: String): Value
}

object ProfigJson {
  var default: ProfigJson = Json

  private var map = Map.empty[String, ProfigJson]

  def types: Set[String] = map.keySet

  register(Json, "conf", "json", "config")
  register(Properties, "prop", "props", "properties")

  def apply(content: String, `type`: Option[String]): Value = {
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
    override def apply(content: String): Value = hierarchical.parse.Json.parse(content)
  }

  object Properties extends ProfigJson {
    override def apply(content: String): Value = hierarchical.parse.Properties.parse(content)
  }
}