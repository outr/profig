# profig

[![Build Status](https://travis-ci.org/outr/profig.svg?branch=master)](https://travis-ci.org/outr/profig)
[![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outr/profig)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outr/profig-core_2.12/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outr/profig-core_2.12)
[![Latest version](https://index.scala-lang.org/outr/profig/profig-core/latest.svg)](https://index.scala-lang.org/outr/profig)

Powerful configuration management for Scala (JSON, properties, command-line arguments, and environment variables)

# Roadmap

## 1.0.0 (In-Progress)

* [X] Merge support
    * [X] Defaults support (only apply if value doesn't already exist)
* [X] Load command-line arguments
* [X] Load environment variables
* [X] Load properties
* [X] Loading case classes
* [X] Storing case classes
* [X] Trait for application startup (JVM and Scala.js)
* [ ] Loading
    * [X] JSON
        * [X] Directly
        * [X] From Disk (JVM-only)
        * [X] From ClassLoader
    * [X] Properties
        * [X] Directly
        * [X] From Disk (JVM-only)
        * [X] From ClassLoader
    * [X] Automatic lookup of default locations
* [ ] Document classes
* [ ] Document README