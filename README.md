# profig

[![Build Status](https://travis-ci.com/outr/profig.svg?branch=master)](https://travis-ci.com/outr/profig)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outr/profig)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outr/profig_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outr/profig_2.12)
[![Latest version](https://index.scala-lang.org/outr/profig/profig/latest.svg)](https://index.scala-lang.org/outr/profig)

Powerful configuration management for Scala (JSON, properties, command-line arguments, and environment variables)

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

* JSON (supported in `profig-core`)
* Properties (support in `profig-core`)
* YAML (supported in `profig-yaml`)
* HOCON (supported in `profig-hocon`)
* XML (supported in `profig-xml`)

# Setup

## SBT Configuration

Profig is published to Sonatype OSS and synchronized to Maven Central supporting JVM and Scala.js on 2.11 and 2.12:

```
libraryDependencies += "com.outr" %% "profig" % "3.0.4"   // Scala
libraryDependencies += "com.outr" %%% "profig" % "3.0.4"  // Scala.js / Cross-Build
```

On the JVM, if you wish to get access to all file formats and extension features of Profig, you can utilize `profig-all`:

```
libraryDependencies += "com.outr" %% "profig-all" % "3.0.4"
```

## Getting Started

Whether you are using this in JVM or JS you need one import to access everything:

`import profig._`

This brings some implicits on specific platforms (for example, loading URLs, Files, Sources, etc. in the JVM) but the
only class you really need be concerned with is `Profig`.

### Initializing

As of version 3.0, you now need to initialize Profig in order to fully utilize it:

`Profig.init()`

This returns a `Future[Unit]` that makes the system fully available when it completes.

Note: an implicit `ExecutionContext` is necessary for init to complete. Under most circumstances you can just use:

`import scala.concurrent.ExecutionContext.Implicits.global`

Or: `import scribe.Execution.global` if you're using Scribe (https://github.com/outr/scribe)

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

`Profig.loadFile(new java.io.File("config.json"))`

This will look for `config.json` on the filesystem, load it as JSON, and merge it into the configuration. The signature
of `loadFile` is: `Profig.loadFile(file: File, mergeType: MergeType = MergeType.Overwrite, errorHandler: Option[Throwable => Unit] = None)`

However, if your application doesn't need very explicit files to be loaded you can load defaults instead:

`Profig.loadConfiguration()`

This will look for any standardized configuration file in the classpath and filesystem and load it into the system.

You can also use `Profig.initConfiguration()` to initialize and load configuration in a single call.

Finally, you can use `Profig.initConfigurationBlocking()` if you want initialization and loading to block before continuing with your application.

### Accessing values

As stated above, system properties and environment variables are automatically loaded into the configuration. So if we
wanted to access the system property "java.version" we can easily do so:

```scala
val javaVersion = Profig("java.version").as[String]
```

You can also load from a higher level as a case class to get more information. For example:

```scala
case class JVMInfo(version: String, specification: Specification)

case class Specification(vendor: String, name: String, version: String)

val info = Profig("java").as[JVMInfo]
```

Configuration files will automatically be loaded from config.json, config.conf, configuration.json, configuration.conf,
application.conf, and application.json if found in the application path or in the classpath.

If default values or `Option` values are defined in the case class they will be used if the value is not available in
the config. However, if any required parameters are missing an exception will be thrown when attempting to read.

### Storing values

Adding values at runtime is almost exactly the same as reading values. For example, if we want to store a basic
configuration:

```scala
case class MyConfig(path: String = "/my/application",
                    timeout: Long = 1000L,
                    username: String = "root",
                    password: String = "password")
                    
Profig.merge(MyConfig(path = "/another/path"))
```

If you would prefer to merge in an object without overwriting existing values you can use `defaults` instead of `merge`:

```scala
Profig.merge(MyConfig(path = "/another/path"), MergeType.Add)
```

### Next steps

This only scratches the surface of the features and functionality Profig provides. For additional information read the
ScalaDocs and the specs: https://github.com/outr/profig/blob/master/core/shared/src/test/scala/spec/ProfigSpec.scala