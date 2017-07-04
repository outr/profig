# profig

[![Build Status](https://travis-ci.org/outr/profig.svg?branch=master)](https://travis-ci.org/outr/profig)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/c0425ea823824cd7ab60659e8b9542dc)](https://www.codacy.com/app/matthicks/profig?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=outr/profig&amp;utm_campaign=Badge_Grade)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/c0425ea823824cd7ab60659e8b9542dc)](https://www.codacy.com/app/matthicks/profig?utm_source=github.com&utm_medium=referral&utm_content=outr/profig&utm_campaign=Badge_Coverage)
[![Stories in Ready](https://badge.waffle.io/outr/profig.png?label=ready&title=Ready)](https://waffle.io/outr/profig)
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
    * [ ] JSON
        * [X] Directly
        * [ ] From Disk (JVM-only)
        * [ ] From ClassLoader
    * [ ] Properties
        * [X] Directly
        * [ ] From Disk (JVM-only)
        * [ ] From ClassLoader
    * [ ] Automatic lookup of default locations
* [ ] Save to disk
* [ ] Support change notification
* [ ] Document classes
* [ ] Document README