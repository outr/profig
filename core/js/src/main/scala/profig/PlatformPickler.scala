package profig

import scala.language.implicitConversions
import scala.scalajs.js

trait PlatformPickler {
  def initProfig(profig: Profig): Unit = {}

  /**
    * Command-line arguments when running under Node (`process.argv` minus the node binary and script); empty in the
    * browser.
    */
  def defaultArguments: List[String] = if (js.typeOf(js.Dynamic.global.process) != "undefined" &&
    !js.isUndefined(js.Dynamic.global.process.argv)) {
    js.Dynamic.global.process.argv.asInstanceOf[js.Array[String]].toList.drop(2)
  } else {
    Nil
  }
}
