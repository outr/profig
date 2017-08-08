name := "profig"
organization in ThisBuild := "com.outr"
version in ThisBuild := "1.1.2-SNAPSHOT"
scalaVersion in ThisBuild := "2.12.3"
crossScalaVersions in ThisBuild := List("2.12.3", "2.11.11")

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