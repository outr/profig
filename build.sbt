name := "profig"
organization in ThisBuild := "com.outr"
version in ThisBuild := "2.0.0-SNAPSHOT"
scalaVersion in ThisBuild := "2.12.4"
crossScalaVersions in ThisBuild := List("2.12.4", "2.11.12")

lazy val profig = crossProject.in(file("."))
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core",
      "io.circe" %%% "circe-generic",
      "io.circe" %%% "circe-parser",
      "io.circe" %%% "circe-generic-extras"
    ).map(_ % "0.8.0"),
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "io.circe" %% "circe-jawn" % "0.8.0",
      "org.scalatest" %%% "scalatest" % "3.0.3" % "test"
    )
  )

lazy val js = profig.js
lazy val jvm = profig.jvm