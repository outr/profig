package profig

import io.circe.Json
import scala.xml._
import moduload.Moduload

object ProfigXML extends Moduload with ProfigJson {
  override def load(): Unit = ProfigJson.register(this, "xml")

  override def error(t: Throwable): Unit = throw t

  override def apply(content: String): Json = {
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

    val root = XML.loadString(content)
    Json.obj(root.label -> toJson(root).getOrElse(Json.Null))
  }
}