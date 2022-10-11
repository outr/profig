# profig

[![CI](https://github.com/outr/profig/actions/workflows/ci.yml/badge.svg)](https://github.com/outr/profig/actions/workflows/ci.yml)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outr/profig)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outr/profig_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outr/profig_2.12)
[![Latest version](https://index.scala-lang.org/outr/profig/profig/latest.svg)](https://index.scala-lang.org/outr/profig)

Powerful configuration management for Scala (JSON, properties, command-line arguments, and environment variables)

# Latest version
@VERSION@

# Justification

In any case where there are existing libraries that accomplish a task it is worthwhile to document the justification for
creating yet another library. This is beneficial both for users to understand how it is differentiated and for the
developers to clarify if there is valid purpose in the endeavor.

In the Scala configuration arena the most popular offering is that of Typesafe Config (https://github.com/typesafehub/config).
While this is a powerful and useful library it is more complicated to work with and less flexible than we'd like. One of
the very specific problems with it is the lack of support for Scala.js, but the larger issue is the distinction that
arises from considering configuration coming from files vs environment variables vs command-line arguments vs any other
origin of configuration that any modern application may want to utilize.

# Features

Our goal is primarily simplicity. A configuration library should do the necessary work and get out of the way of the
developer and let them get their job done. To this end we support a unified configuration merging command-line arguments,
environment variables, system properties, and configuration files to provide maximum flexibility of defining, defaulting,
and overriding configuration in your application.

# File Formats

* JSON
* Properties
* YAML
* HOCON
* XML

# Setup

## SBT Configuration

Profig is published to Sonatype OSS and synchronized to Maven Central supporting JVM and Scala.js on 2.11, 2.12, 2.13, and Scala 3.x:

```
libraryDependencies += "com.outr" %% "profig" % "@VERSION@"   // Scala
libraryDependencies += "com.outr" %%% "profig" % "@VERSION@"  // Scala.js / Cross-Build
```

## Getting Started

Whether you are using this in JVM or JS you need one import to access everything:

```scala mdoc
import profig._
```

This brings some implicits on specific platforms (for example, loading URLs, Files, Sources, etc. in the JVM) but the
only class you really need be concerned with is `Profig`.

### Initializing

As of version 3.0, you now need to initialize Profig in order to fully utilize it:

```scala mdoc
Profig.init()
```

### Loading Command-Line arguments

When your application starts it is reasonable to want to allow execution of the application to override existing
configuration via the command-line. In order to effectively do this we can simply invoke `Profig.merge(args)` within our
main method. This will merge all command-line arguments into Profig.

Note that the signature of `merge` is `def merge(json: Json, ``type``: MergeType = MergeType.Overwrite): Unit`. If you
set the type to `MergeType.Add`, existing configuration will not be overwritten. This is useful for default configuration
loading.

### Loading Files

Profig supports many configuration formats and can look in the classpath as well as the filesystem to find configuration
to load. Of course, this is only supported on the JVM, but to load a file simply call:

```scala mdoc
Profig.loadFile(new java.io.File("config.json"))
```

This will look for `config.json` on the filesystem, load it as JSON, and merge it into the configuration. The signature
of `loadFile` is: `Profig.loadFile(file: File, mergeType: MergeType = MergeType.Overwrite, errorHandler: Option[Throwable => Unit] = None)`

However, if your application doesn't need very explicit files to be loaded you can load defaults instead:

```scala mdoc
Profig.loadConfiguration()
```

This will look for any standardized configuration file in the classpath and filesystem and load it into the system.

You can also use `Profig.initConfiguration()` to initialize and load configuration in a single call.

Finally, you can use `Profig.initConfigurationBlocking()` if you want initialization and loading to block before continuing with your application.

### Accessing values

As stated above, system properties and environment variables are automatically loaded into the configuration. So if we
wanted to access the system property "java.version" we can easily do so:

```scala mdoc
val javaVersion = Profig("java.version").as[String]
```

You can also load from a higher level as a case class to get more information. For example:

```scala mdoc
import fabric.rw._

case class JVMInfo(version: String, specification: Specification)

object JVMInfo {
  implicit val rw: RW[JVMInfo] = RW.gen
}

case class Specification(vendor: String, name: String, version: String)

object Specification {
  implicit val rw: RW[Specification] = RW.gen
}

val info = Profig("java").as[JVMInfo]
```

Configuration files will automatically be loaded from config.json, config.conf, configuration.json, configuration.conf,
application.conf, and application.json if found in the application path or in the classpath.

If default values or `Option` values are defined in the case class they will be used if the value is not available in
the config. However, if any required parameters are missing an exception will be thrown when attempting to read.

### Storing values

Adding values at runtime is almost exactly the same as reading values. For example, if we want to store a basic
configuration:

```scala mdoc
import fabric._

case class MyConfig(path: String = "/my/application",
                    timeout: Long = 1000L,
                    username: String = "root",
                    password: String = "password")
                    
object MyConfig {
  implicit val rw: RW[MyConfig] = RW.gen
}

val json: Json = MyConfig(path = "/another/path").json
Profig.merge(json)
```

If you prefer to merge in an object without overwriting existing values you can use `defaults` instead of `merge`:

```scala mdoc
val myConfig: Json = MyConfig(path = "/another/path").json
Profig.merge(json, MergeType.Add)
```

### Next steps

This only scratches the surface of the features and functionality Profig provides. For additional information read the
ScalaDocs and the specs: https://github.com/outr/profig/blob/master/core/shared/src/test/scala/spec/ProfigSpec.scala