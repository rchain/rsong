import Dependencies._
import Settings._
import TodoListPlugin._
import NativePackagerHelper._
import com.typesafe.sbt.packager.docker._

lazy val compilerSettings = CompilerSettings.options ++ Seq(
  crossScalaVersions := Seq("2.11.12", scalaVersion.value)
)

lazy val acq = (project in file("acq")).
  settings(Settings.proxy: _*).
  configs(IntegrationTest).
  settings(Settings.acq: _*).
  dependsOn(core).
  settings(libraryDependencies ++= Dep.acq)

lazy val proxy = (project in file("proxy")).
  settings(Settings.proxy: _*).
  configs(IntegrationTest).
  settings(Defaults.itSettings: _*).
  dependsOn(core).
  enablePlugins(JavaAppPackaging, BuildInfoPlugin).
  settings(libraryDependencies ++= Dep.proxy)

lazy val core = (project in file("core")).
  settings(Settings.proxy: _*).
  configs(IntegrationTest).
  settings(libraryDependencies ++= Dep.core).
  settings(Settings.core: _*)

enablePlugins(JavaAppPackaging)

enablePlugins(UniversalPlugin)

compileWithTodolistSettings
