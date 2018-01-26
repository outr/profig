package profig

import scala.language.experimental.macros

/**
  * Provides a convenience mix-in to JVM and Scala.js application entry points to load supplied arguments before
  * invoking the application.
  */
trait ConfigApplication {
  def main(args: Array[String]): Unit

  /**
    * Loads args into Profig and then calls `run()` to start the application.
    */
  def start(args: Seq[String]): Unit = macro Macros.start

  /**
    * Application entry point. This will be invoked immediately after initialization is successful in `main`.
    */
  protected def run(): Unit
}