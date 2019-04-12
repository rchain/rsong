import Dependencies._
import TodoListPlugin._

lazy val compilerSettings = CompilerSettings.options ++ Seq(crossScalaVersions := Seq("2.11.12", scalaVersion.value))

lazy val acq = (project in file("acq"))
  .configs(IntegrationTest extend Test)
  .settings(Settings.proxy: _*)
  .settings(Defaults.itSettings)
  .settings(Settings.acq: _*)
  .dependsOn(core % "compile->compile;test->test")
  .settings(libraryDependencies ++= Dep.acq)

lazy val proxy = (project in file("proxy"))
  .configs(IntegrationTest extend Test)
  .settings(Settings.proxy: _*)
  .settings(Defaults.itSettings)
  .dependsOn(core % "compile -> compile;test->test")
  .dependsOn(acq % "compile -> compile;test->test")
  .enablePlugins(JavaAppPackaging, BuildInfoPlugin)
  .settings(libraryDependencies ++= Dep.proxy)

lazy val core = (project in file("core"))
  .settings(Settings.proxy: _*)
  .configs(IntegrationTest)
  .settings(libraryDependencies ++= Dep.core)
  .settings(Settings.core: _*)

enablePlugins(JavaAppPackaging)

enablePlugins(UniversalPlugin)

compileWithTodolistSettings
