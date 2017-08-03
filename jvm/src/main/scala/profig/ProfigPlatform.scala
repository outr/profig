package profig

import java.util.concurrent.atomic.AtomicBoolean

/**
  * Platform-specific initialization for JVM
  */
object ProfigPlatform {
  val initialized: AtomicBoolean = new AtomicBoolean(false)

  /**
    * Called upon initialization of Config at first use. Attempts to load the following from the classloader and then
    * from the local filesystem:
    *   - config.json
    *   - config.conf
    *   - configuration.json
    *   - configuration.conf
    *   - application.conf
    *   - application.json
    * The order matters, so later configuration files will overwrite former. Similarly, files will overwrite
    * configuration in the classloader.
    *
    * Additionally, defaults can be defined to avoid overriding system properties and environment variables using the
    * following files:
    *   - defaults.json
    *   - defaults.conf
    * These files will never overwrite existing settings and is a great way to define defaults for your application
    * while avoiding replacing user-defined values.
    */
  def init(): Unit = ProfigJVM.init()
}