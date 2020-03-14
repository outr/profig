package profig

import io.circe.Json

/**
  * Platform-specific initialization for JVM
  */
object ProfigPlatform {
  val yamlConversion: Option[String => Json] = Some(ProfigLookupPath.yamlString2Json)
  def isJS: Boolean = false
  def isJVM: Boolean = true

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
}