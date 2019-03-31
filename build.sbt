import Dependencies._
import Settings._
import TodoListPlugin._
import NativePackagerHelper._
import com.typesafe.sbt.packager.docker._

lazy val compilerSettings = CompilerSettings.options ++ Seq(
  crossScalaVersions := Seq("2.11.12", scalaVersion.value)
)
lazy val acq = (project in file("acq")).
  settings(Settings.global: _*).
  settings(Settings.acq: _*).
  dependsOn(core).
  settings(libraryDependencies ++= Dep.acq)

lazy val proxy = (project in file("proxy")).
  settings(Settings.global: _*).
  settings(Settings.proxy: _*).
  dependsOn(core).
  enablePlugins(JavaAppPackaging, BuildInfoPlugin).
  settings(libraryDependencies ++= Dep.proxy)

lazy val core = (project in file("core")).
  settings(Settings.global: _*).
  settings(Settings.core: _*).
  configs(Test). 
  settings(libraryDependencies ++= Dep.core)

enablePlugins(JavaAppPackaging)

enablePlugins(UniversalPlugin)

compileWithTodolistSettings
