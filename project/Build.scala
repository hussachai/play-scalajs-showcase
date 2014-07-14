import sbt._
import Keys._
import play.Play._
import scala.scalajs.sbtplugin.ScalaJSPlugin._
import ScalaJSKeys._
import com.typesafe.sbt.packager.universal.UniversalKeys
import com.typesafe.sbteclipse.core.EclipsePlugin.EclipseKeys

import play.Play.autoImport._
import PlayKeys._

object ApplicationBuild extends Build with UniversalKeys {


  val scalajsOutputDir = Def.settingKey[File]("directory for javascript files output by scalajs")

  override def rootProject = Some(scalajvm)

  val sharedSrcDir = "scala"

  lazy val scalajvm = Project(
    id = "scalajvm",
    base = file("scalajvm")
  ) enablePlugins (play.PlayScala) settings (scalajvmSettings: _*) aggregate (scalajs) aggregate (sharedScala)

  lazy val scalajs = Project(
    id   = "scalajs",
    base = file("scalajs")
  ) settings (scalajsSettings: _*) aggregate (sharedScala)

  lazy val sharedScala = Project(
    id = "scala",
    base = file(sharedSrcDir)
  ) settings (sharedScalaSettings: _*) 

  lazy val scalajvmSettings =
    Seq(
      name := "play-example",
      version := Versions.app,
      scalaVersion := Versions.scala,
      routesImport += "controllers._",
      scalajsOutputDir := (crossTarget in Compile).value / "classes" / "public" / "javascripts",
      compile in Compile <<= (compile in Compile) dependsOn (fastOptJS in (scalajs, Compile)),
      dist <<= dist dependsOn (fullOptJS in (scalajs, Compile)),
      libraryDependencies ++= Dependencies.scalajvm,
      commands += preStartCommand,
      EclipseKeys.skipParents in ThisBuild := false
    ) ++ (
      // ask scalajs project to put its outputs in scalajsOutputDir
      Seq(packageExternalDepsJS, packageInternalDepsJS, packageExportedProductsJS, packageLauncher, fastOptJS, fullOptJS) map { packageJSKey =>
        crossTarget in (scalajs, Compile, packageJSKey) := scalajsOutputDir.value
      }
    ) ++ sharedDirectorySettings

  lazy val scalajsSettings =
    scalaJSSettings ++ Seq(
      name := "scalajs-example",
      version := Versions.app,
      scalaVersion := Versions.scala,
      persistLauncher := true,
      persistLauncher in Test := false,
      libraryDependencies ++= Dependencies.scalajs
    ) ++ sharedDirectorySettings

  lazy val sharedScalaSettings =
    Seq(
      name := "shared-scala-example",
      scalaVersion := Versions.scala,
      //scalaSource in Compile := baseDirectory.value,
      EclipseKeys.skipProject := true,
      libraryDependencies ++= Dependencies.shared
    )

  lazy val sharedDirectorySettings = Seq(
    unmanagedSourceDirectories in Compile += new File((file(".") / sharedSrcDir / "src" / "main" / "scala").getCanonicalPath),
    unmanagedSourceDirectories in Test += new File((file(".") / sharedSrcDir / "src" / "test" / "scala").getCanonicalPath),
    unmanagedResourceDirectories in Compile += file(".") / sharedSrcDir / "src" / "main" / "resources",
    unmanagedResourceDirectories in Test += file(".") / sharedSrcDir / "src" / "test" / "resources"
  )

  // Use reflection to rename the 'start' command to 'play-start'
  Option(play.Play.playStartCommand.getClass.getDeclaredField("name")) map { field =>
    field.setAccessible(true)
    field.set(playStartCommand, "play-start")
  }

  // The new 'start' command optimises the JS before calling the Play 'start' renamed 'play-start'
  val preStartCommand = Command.args("start", "<port>") { (state: State, args: Seq[String]) =>
    Project.runTask(fullOptJS in (scalajs, Compile), state)
    state.copy(remainingCommands = ("play-start " + args.mkString(" ")) +: state.remainingCommands)
  }
}

object Dependencies {
  val shared = Seq()

  val scalajvm = Seq(
    filters,
    jdbc,
    anorm,
    "com.typesafe.slick" %% "slick" % "2.1.0-M2",
    "com.typesafe.play" %% "play-slick" % "0.8.0-M1",
    "com.lihaoyi" %% "upickle" % "0.1.3",
    "org.webjars" % "jquery" % "1.9.0"
  ) ++ shared

  val scalajs = Seq(
    "org.scala-lang.modules.scalajs" %%% "scalajs-dom" % Versions.scalajsDom,
    "org.scala-lang.modules.scalajs" %% "scalajs-jasmine-test-framework" % scalaJSVersion % "test",
    "com.lihaoyi" %%% "upickle" % "0.1.3",
    "com.scalatags" %%% "scalatags" % "0.3.0",
    "com.scalarx" %%% "scalarx" % "0.2.5",
    "org.scala-lang.modules.scalajs" %%% "scalajs-jquery" % "0.6"
  ) ++ shared
}

object Versions {
  val app = "0.1.0-SNAPSHOT"
  val scala = "2.11.1"
  val scalajsDom = "0.6"
}
