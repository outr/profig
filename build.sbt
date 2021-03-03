import sbtcrossproject.CrossPlugin.autoImport.crossProject
import sbtcrossproject.CrossType

// Scala versions
val scala213 = "2.13.4"
val scala212 = "2.12.12"
//val scala3 = "3.0.0-M3"
val allScalaVersions = List(scala213, scala212) //, scala3)
val scala2Versions = List(scala213, scala212)
val compatScalaVersions = List(scala213, scala212)

organization in ThisBuild := "com.outr"
version in ThisBuild := "3.2.0-SNAPSHOT"
scalaVersion in ThisBuild := scala213
scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation", "-feature")

publishTo in ThisBuild := sonatypePublishTo.value
publishConfiguration in ThisBuild := publishConfiguration.value.withOverwrite(true)
sonatypeProfileName in ThisBuild := "com.outr"
publishMavenStyle in ThisBuild := true
licenses in ThisBuild := Seq("MIT" -> url("https://github.com/outr/profig/blob/master/LICENSE"))
sonatypeProjectHosting in ThisBuild := Some(xerial.sbt.Sonatype.GitHubHosting("outr", "profig", "matt@outr.com"))
homepage in ThisBuild := Some(url("https://github.com/outr/profig"))
scmInfo in ThisBuild := Some(
  ScmInfo(
    url("https://github.com/outr/profig"),
    "scm:git@github.com:outr/profig.git"
  )
)
developers in ThisBuild := List(
  Developer(id="darkfrog", name="Matt Hicks", email="matt@matthicks.com", url=url("http://matthicks.com"))
)

val fabric = "1.0.0-SNAPSHOT"
val collectionCompat = "2.4.2"
val reactify = "4.0.3"
val scalaXMLVersion = "2.0.0-M5"
val scalatestVersion = "3.2.5"

// Used for HOCON support
val typesafeConfig = "1.4.1"

// Used for YAML support
val jacksonVersion = "2.12.1"

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
      "org.scala-lang.modules" %%% "scala-collection-compat" % collectionCompat
    ),
    crossScalaVersions := allScalaVersions
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % scalatestVersion % "test"
    )
  )
  .jsSettings(
    test := {},                 // Temporary work-around for ScalaTest not working with Scala.js on Dotty
    libraryDependencies ++= (
      if (isDotty.value) {      // Temporary work-around for ScalaTest not working with Scala.js on Dotty
        Nil
      } else {
        List("org.scalatest" %%% "scalatest" % scalatestVersion % "test")
      }
    )
  )

lazy val coreJS = core.js
lazy val coreJVM = core.jvm
