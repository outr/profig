import scala.concurrent.Future

package object profig extends SharedJSONConversions {
  def initProfig(loadModules: Boolean): Unit = {
    Future.successful(())
  }
}