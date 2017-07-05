package profig

/**
  * Provides a convenience mix-in to JVM and Scala.js application entry points to load supplied arguments before
  * invoking the application.
  */
trait ConfigApplication {
  /**
    * Loads args into Config and then calls `run()` to start the application.
    */
  def main(args: Array[String]): Unit = {
    Config.merge(args)
    run()
  }

  /**
    * Application entry point. This will be invoked immediately after initialization is successful in `main`.
    */
  protected def run(): Unit
}