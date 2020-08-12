package profig

import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import io.circe.Json
import moduload.Moduload

import scala.concurrent.{ExecutionContext, Future}

object ProfigHocon extends Moduload with ProfigJson {
  override def load()(implicit ec: ExecutionContext): Future[Unit] = {
    ProfigJson.register(this, "conf", "config", "hocon")
    Future.successful(())
  }

  override def error(t: Throwable): Unit = throw t

  override def apply(content: String): Json = {
    val conf = ConfigFactory.parseString(content).resolve()
    val jsonString = conf.root().render(ConfigRenderOptions.concise())
    ProfigJson.Circe(jsonString)
  }
}