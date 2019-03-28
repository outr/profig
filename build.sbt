import sbtcrossproject.{CrossType, crossProject}

organization in ThisBuild := "com.outr"
version in ThisBuild := "2.3.5"
scalaVersion in ThisBuild := "2.12.8"
crossScalaVersions in ThisBuild := List("2.12.8", "2.11.12")
scalacOptions in ThisBuild ++= Seq("-unchecked", "-deprecation")

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
  Developer(id="darkfrog", name="Matt Hicks", email="matt@matthicks.", url=url("http://matthicks.com"))
)

val circeVersion = "0.11.1"
val circeYamlVersion = "0.9.0"
val shoconVersion = "0.4.1"
val scalaXMLVersion = "1.1.1"
val scalatestVersion = "3.0.5"

lazy val root = project.in(file("."))
  .aggregate(irPatch, macrosJS, macrosJVM, coreJS, coreJVM, inputJS, inputJVM)
  .settings(
    name := "profig",
    publish := {},
    publishLocal := {}
  )

lazy val irPatch = project.in(file("irpatch"))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    libraryDependencies += "io.circe" %%% "circe-parser" % circeVersion
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
      "io.circe" %%% "circe-generic-extras"
    ).map(_ % circeVersion),
    libraryDependencies ++= Seq(
      "org.akka-js" %%% "shocon-parser" % shoconVersion,
      "org.scala-lang.modules" %%% "scala-xml" % scalaXMLVersion,
      "io.circe" %% "circe-jawn" % circeVersion,
      "io.circe" %% "circe-yaml" % circeYamlVersion,
      "org.scala-lang" % "scala-reflect" % scalaVersion.value
    )
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
    )
  )

lazy val coreJS = core.js
lazy val coreJVM = core.jvm

lazy val input = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("input"))
  .dependsOn(core)
  .settings(
    name := "profig-input",
    libraryDependencies ++= Seq(
      "org.scala-lang" % "scala-reflect" % scalaVersion.value,
      "org.scalatest" %%% "scalatest" % scalatestVersion % "test"
    )
  )

lazy val inputJS = input.js
lazy val inputJVM = input.jvm