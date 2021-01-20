package profig

import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.JSON

trait PlatformPickler {
  implicit def jsObject2Json(obj: js.Object): Json = Json.parse(JSON.stringify(obj))
  implicit def Json2JsObject(json: Json): js.Object = JSON.parse(json.render()).asInstanceOf[js.Object]

  def initProfig(loadModules: Boolean): Unit = {}
}