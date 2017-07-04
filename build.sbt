name := "profig"
organization in ThisBuild := "com.outr"
version in ThisBuild := "1.0.0-SNAPSHOT"
scalaVersion in ThisBuild := "2.12.2"
crossScalaVersions in ThisBuild := List("2.12.2", "2.11.11")

lazy val root = crossProject.in(file("."))
    .settings(
      libraryDependencies ++= Seq(
        "io.circe" %%% "circe-core",
        "io.circe" %%% "circe-generic",
        "io.circe" %%% "circe-parser",
        "io.circe" %%% "circe-optics"
      ).map(_ % "0.8.0"),
      libraryDependencies ++= Seq(
        "org.scalatest" %%% "scalatest" % "3.0.3" % "test"
      )
    )

lazy val js = root.js
lazy val jvm = root.jvm