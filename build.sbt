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
version in ThisBuild := "3.1.3-SNAPSHOT"
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

val uPickle = "1.2.3"
val moduload = "1.1.0"
val collectionCompat = "2.4.1"
val reactify = "4.0.3"
val scalaXMLVersion = "2.0.0-M4"
val scalatestVersion = "3.2.3"

// Used for HOCON support
val typesafeConfig = "1.4.1"

// Used for YAML support
val jacksonVersion = "2.12.1"

lazy val root = project.in(file("."))
  .aggregate(coreJS, coreJVM, xml, hocon, yaml, all)
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
      "com.lihaoyi" %%% "upickle" % uPickle,
      "org.scala-lang.modules" %%% "scala-collection-compat" % collectionCompat
    ),
    crossScalaVersions := allScalaVersions
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "com.outr" %% "moduload" % moduload,
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

lazy val xml = project
  .in(file("xml"))
  .settings(
    name := "profig-xml",
    libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % scalaXMLVersion,
    crossScalaVersions := allScalaVersions
  )
  .dependsOn(core.jvm)

lazy val hocon = project
  .in(file("hocon"))
  .settings(
    name := "profig-hocon",
    libraryDependencies += "com.typesafe" % "config" % typesafeConfig,
    crossScalaVersions := allScalaVersions
  )
  .dependsOn(core.jvm)

lazy val yaml = project
  .in(file("yaml"))
  .settings(
    name := "profig-yaml",
    libraryDependencies += "com.fasterxml.jackson.dataformat" % "jackson-dataformat-yaml" % jacksonVersion,
    crossScalaVersions := allScalaVersions
  )
  .dependsOn(core.jvm)

lazy val all = project
  .in(file("all"))
  .settings(
    name := "profig-all",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % scalatestVersion % "test"
    ),
    crossScalaVersions := allScalaVersions
  )
  .dependsOn(coreJVM, xml, hocon, yaml)
