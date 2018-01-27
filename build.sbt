import sbtcrossproject.{CrossType, crossProject}

organization in ThisBuild := "com.outr"
version in ThisBuild := "2.0.0-SNAPSHOT"
scalaVersion in ThisBuild := "2.12.4"
crossScalaVersions in ThisBuild := List("2.12.4", "2.11.12")
scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")

val circeVersion = "0.9.1"
val scalatestVersion = "3.0.4"

lazy val root = project.in(file("."))
  .aggregate(macrosJS, macrosJVM, coreJS, coreJVM)
  .settings(
    name := "profig",
    publish := {},
    publishLocal := {}
  )

lazy val macros = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("macros"))
  .settings(
    name := "profig-macros",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    )
  )

lazy val macrosJS = macros.js
lazy val macrosJVM = macros.jvm

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("core"))
  .dependsOn(macros)
  .settings(
    name := "profig",
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core",
      "io.circe" %%% "circe-generic",
      "io.circe" %%% "circe-parser",
      "io.circe" %%% "circe-generic-extras"
    ).map(_ % circeVersion),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-jawn" % circeVersion,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scalatest" %%% "scalatest" % scalatestVersion % "test"
    )
  )

lazy val coreJS = core.js
lazy val coreJVM = core.jvm