package profig

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import moduload.Moduload

object ProfigYaml extends Moduload with ProfigJson {
  override def load(): Unit = ProfigJson.register(this, "yaml", "yml")

  override def error(t: Throwable): Unit = throw t

  override def apply(content: String): Json = {
    val reader = new ObjectMapper(new YAMLFactory)
    val obj = reader.readValue(content, classOf[java.lang.Object])
    val writer = new ObjectMapper()
    val jsonString = writer.writeValueAsString(obj)
    Json.parse(jsonString)
  }
}
