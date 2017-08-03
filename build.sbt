name := "profig"
organization in ThisBuild := "com.outr"
version in ThisBuild := "1.1.0"
scalaVersion in ThisBuild := "2.12.3"
crossScalaVersions in ThisBuild := List("2.12.3", "2.11.11")

lazy val macros = crossProject.in(file("macros"))
  .settings(
    name := "profig-macros",
    libraryDependencies ++= Seq(
      "io.circe" %%% "circe-core",
      "io.circe" %%% "circe-generic",
      "io.circe" %%% "circe-parser",
      "io.circe" %%% "circe-generic-extras"
    ).map(_ % "0.8.0"),
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "io.circe" %% "circe-jawn" % "0.8.0"
    )
  )

lazy val macrosJS = macros.js
lazy val macrosJVM = macros.jvm

lazy val profig = crossProject.in(file("."))
  .settings(
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scalatest" %%% "scalatest" % "3.0.3" % "test"
    )
  )
  .dependsOn(macros)

lazy val js = profig.js
lazy val jvm = profig.jvm