import scala.concurrent.{ExecutionContext, Future}

package object profig extends SharedJSONConversions {
  def initProfig(loadModules: Boolean)(implicit ec: ExecutionContext): Future[Unit] = {
    Future.successful(())
  }
}