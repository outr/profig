# profig

[![Build Status](https://travis-ci.org/outr/profig.svg?branch=master)](https://travis-ci.org/outr/profig)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outr/profig)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outr/profig_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outr/profig_2.12)
[![Latest version](https://index.scala-lang.org/outr/profig/profig/latest.svg)](https://index.scala-lang.org/outr/profig)

Powerful configuration management for Scala (JSON, properties, command-line arguments, and environment variables)

# Justification

In any case where there are existing libraries that accomplish a task it is worthwhile to document the justification for
creating yet another library. This is beneficial both for users to understand how it is differentiated as well as for
the developers to clarify there is valid purpose in the endeavor.

In the Scala configuration arena the most popular offering is that of Typesafe Config (https://github.com/typesafehub/config).
While this is a powerful and useful library it is more complicated to work with and less flexible than we'd like. One of
the very specific problems with it is the lack of support for Scala.js.

# Features

Our goal is primarily simplicity. A configuration library should do the necessary work and get out of the way of the
developer and let them get their job done. To this end we support a unified configuration merging command-line arguments,
environment variables, system properties, and configuration files to provide maximum flexibility of defining, defaulting,
and overriding configuration in your application.

# Setup

## SBT Configuration

Profig is published to Sonatype OSS and synchronized to Maven Central supporting JVM and Scala.js on 2.11 and 2.12:

```
libraryDependencies += "com.outr" %% "profig" % "1.0.2"   // Scala
libraryDependencies += "com.outr" %%% "profig" % "1.0.2"  // Scala.js / Cross-Build
```

## Getting Started

Whether you are using this in JVM or JS you need one import to access everything:

`import profig._`

This brings some implicits on specific platforms (for example, loading URLs, Files, Sources, etc. in the JVM) but the
only class you really need be concerned with is `Config`.

### Loading Command-Line arguments

When your application starts it is reasonable to want to allow execution of the application to override existing configuration
via the command-line. In order to effectively do this we can simply invoke `Config.merge(args)` within our main method.

For a more managed representation this can be handled for you by using the `ConfigApplication` mix-in:

```scala
object MyApplication extends ConfigApplication {
  override def run(): Unit = // this is now the main entry point invoked after command-line arguments are loaded
}
```

### Accessing values

As stated above, system properties and environment variables are automatically loaded into the configuration. So if we
wanted to access the system property "java.version" we can easily do so:

```scala
val javaVersion = Config("java.version").as[String]
```

You can also load from a higher level as a case class to get more information. For example:

```scala
case class JVMInfo(version: String, specification: Specification)

case class Specification(vendor: String, name: String, version: String)

val info = Config("java").as[JVMInfo]
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
                    
Config.merge(MyConfig(path = "/another/path"))
```

If you would prefer to merge in an object without overwriting existing values you can use `defaults` instead of `merge`:

```scala
Config.defaults(MyConfig(path = "/another/path"))
```

### Next steps

This only scratches the surface of the features and functionality Profig provides. For additional information read the
ScalaDocs and the specs: https://github.com/outr/profig/blob/master/shared/src/test/scala/spec/ConfigSpec.scala

# Roadmap

## 1.1.0 (In-Progress)

* [ ] HOCON support (integrate https://github.com/unicredit/shocon)
* [ ] Migration support for Typesafe Config

## 1.0.0 (Released 07.04.2017)

* [X] Merge support
    * [X] Defaults support (only apply if value doesn't already exist)
* [X] Load command-line arguments
* [X] Load environment variables
* [X] Load properties
* [X] Loading case classes
* [X] Storing case classes
* [X] Trait for application startup (JVM and Scala.js)
* [X] Loading
    * [X] JSON
        * [X] Directly
        * [X] From Disk (JVM-only)
        * [X] From ClassLoader
    * [X] Properties
        * [X] Directly
        * [X] From Disk (JVM-only)
        * [X] From ClassLoader
    * [X] Automatic lookup of default locations
* [X] Document classes
* [X] Document README