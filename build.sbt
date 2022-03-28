import sbtcrossproject.CrossPlugin.autoImport.crossProject
import sbtcrossproject.CrossType

// Scala versions
val scala213 = "2.13.8"
val scala212 = "2.12.15"
val scala3 = List("3.1.1")
val scala2 = List(scala213, scala212)
val allScalaVersions = scala2 ::: scala3

ThisBuild / organization := "com.outr"
ThisBuild / version := "3.3.3"
ThisBuild / scalaVersion := scala213
ThisBuild / scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature")

ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"
ThisBuild / sonatypeRepository := "https://s01.oss.sonatype.org/service/local"
ThisBuild / publishTo := sonatypePublishTo.value
ThisBuild / publishConfiguration := publishConfiguration.value.withOverwrite(true)
ThisBuild / sonatypeProfileName := "com.outr"
ThisBuild / publishMavenStyle := true
ThisBuild / licenses := Seq("MIT" -> url("https://github.com/outr/profig/blob/master/LICENSE"))
ThisBuild / sonatypeProjectHosting := Some(xerial.sbt.Sonatype.GitHubHosting("outr", "profig", "matt@outr.com"))
ThisBuild / homepage := Some(url("https://github.com/outr/profig"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/outr/profig"),
    "scm:git@github.com:outr/profig.git"
  )
)
ThisBuild / developers := List(
  Developer(id="darkfrog", name="Matt Hicks", email="matt@matthicks.com", url=url("https://matthicks.com"))
)

val fabric: String = "1.2.9"

val collectionCompat: String = "2.7.0"

val reactify: String = "4.0.7"

val scalaTest: String = "3.2.11"

// Used for HOCON support
val typesafeConfig = "1.4.1"

// Used for YAML and XML support
val jacksonVersion = "2.12.3"

lazy val root = project.in(file("."))
  .aggregate(coreJS, coreJVM)
  .settings(
    name := "profig",
    publish := {},
    publishLocal := {}
  )

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("core"))
  .settings(
    name := "profig",
    libraryDependencies ++= Seq(
      "com.outr" %%% "fabric-parse" % fabric,
      "org.scala-lang.modules" %%% "scala-collection-compat" % collectionCompat,
      "org.scalatest" %% "scalatest" % scalaTest % "test"
    ),
    crossScalaVersions := allScalaVersions,
    test / fork := true
  )

lazy val coreJS = core.js
lazy val coreJVM = core.jvm
