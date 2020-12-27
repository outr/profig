import sbtcrossproject.CrossPlugin.autoImport.crossProject
import sbtcrossproject.CrossType

// Scala versions
val scala213 = "2.13.4"
val scala212 = "2.12.12"
val scala3 = "3.0.0-M3"
val allScalaVersions = List(scala213, scala212, scala3)
val scala2Versions = List(scala213, scala212)
val compatScalaVersions = List(scala213, scala212)

organization in ThisBuild := "com.outr"
version in ThisBuild := "3.0.4-SNAPSHOT"
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

val moduload = "1.0.4"
val circeVersion = "0.14.0-M2"
val circeGenericExtrasVersion = "0.13.0"
val circeYamlVersion = "0.13.1"
val collectionCompat = "2.3.2"
val reactify = "4.0.3"
val scalaXMLVersion = "2.0.0-M3"
val scalatestVersion = "3.2.3"

// Used for HOCON support
val typesafeConfig = "1.4.1"

lazy val root = project.in(file("."))
  .aggregate(irPatch, macrosJS, macrosJVM, coreJS, coreJVM, xml, hocon, yaml, inputJS, inputJVM, live, all)
  .settings(
    name := "profig",
    publish := {},
    publishLocal := {}
  )

lazy val irPatch = project.in(file("irpatch"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    libraryDependencies += "io.circe" %%% "circe-parser" % circeVersion,
    crossScalaVersions in ThisBuild := allScalaVersions
  )

lazy val macros = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("macros"))
  .settings(
    name := "profig-macros",
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core",
      "io.circe" %%% "circe-generic",
      "io.circe" %%% "circe-parser",
    ).map(_ % circeVersion),
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-generic-extras" % circeGenericExtrasVersion,
      "io.circe" %% "circe-jawn" % circeVersion,
      "org.scala-lang.modules" %%% "scala-collection-compat" % collectionCompat,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    ),
    crossScalaVersions in ThisBuild := allScalaVersions
  )
  .jsSettings(
    manipulateBytecode in Compile := {    // Allows access to Json parsing at compile-time (for use with Macros)
      val result = (manipulateBytecode in Compile).value

      val classDir = (classDirectory in Compile).value
      val irPatchesDirs = (products in (irPatch, Compile)).value

      def recursiveCopy(file: File, dir: File): Unit = if (file.isFile && file.getName.endsWith(".sjsir")) {
        dir.mkdirs()
        val output = dir / file.getName
        IO.copyFile(file, output)
      } else if (file.isDirectory) {
        file.listFiles().foreach(recursiveCopy(_, dir / file.getName))
      }
      irPatchesDirs.foreach(recursiveCopy(_, classDir))

      result
    }
  )

lazy val macrosJS = macros.js
lazy val macrosJVM = macros.jvm

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("core"))
  .dependsOn(macros % "compile->compile;test->test")
  .settings(
    name := "profig",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % scalatestVersion % "test"
    ),
    crossScalaVersions in ThisBuild := allScalaVersions
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "com.outr" %% "moduload" % moduload
    )
  )

lazy val coreJS = core.js
lazy val coreJVM = core.jvm

lazy val xml = project
  .in(file("xml"))
  .settings(
    name := "profig-xml",
    libraryDependencies += "org.scala-lang.modules" %%% "scala-xml" % scalaXMLVersion,
    crossScalaVersions in ThisBuild := allScalaVersions
  )
  .dependsOn(core.jvm)

lazy val hocon = project
  .in(file("hocon"))
  .settings(
    name := "profig-hocon",
    libraryDependencies += "com.typesafe" % "config" % typesafeConfig,
    crossScalaVersions in ThisBuild := allScalaVersions
  )
  .dependsOn(core.jvm)

lazy val yaml = project
  .in(file("yaml"))
  .settings(
    name := "profig-yaml",
    libraryDependencies += "io.circe" %% "circe-yaml" % circeYamlVersion,
    crossScalaVersions in ThisBuild := allScalaVersions
  )
  .dependsOn(core.jvm)

lazy val input = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("input"))
  .dependsOn(core)
  .settings(
    name := "profig-input",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scalatest" %%% "scalatest" % scalatestVersion % "test"
    ),
    crossScalaVersions in ThisBuild := allScalaVersions
  )

lazy val inputJS = input.js
lazy val inputJVM = input.jvm

lazy val live = project
  .in(file("live"))
  .dependsOn(coreJVM)
  .settings(
    name := "profig-live",
    libraryDependencies ++= Seq(
      "com.outr" %% "reactify" % reactify,
      "org.scalatest" %% "scalatest" % scalatestVersion % "test"
    ),
    crossScalaVersions in ThisBuild := allScalaVersions
  )

lazy val all = project
  .in(file("all"))
  .settings(
    name := "profig-all",
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % scalatestVersion % "test"
    ),
    crossScalaVersions in ThisBuild := allScalaVersions
  )
  .dependsOn(coreJVM, xml, hocon, yaml, inputJVM)