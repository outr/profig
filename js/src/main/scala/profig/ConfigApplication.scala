package profig

import scala.scalajs.js.JSApp

trait ConfigApplication extends JSApp {
  override def main(): Unit = {
    Config.init(Nil)
    run()
  }

  protected def run(): Unit
}