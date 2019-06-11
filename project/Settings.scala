import sbt._
import Keys._


object Settings {
  lazy val global = Seq(
    organization := "coop.rchain.rsong",
    scalaVersion := "2.12.8",
    publishMavenStyle := true,
    publishArtifact in Test := false,
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots"),
      "jitpack" at "https://jitpack.io"),
    testOptions in Test += Tests.Argument("-oD"), 
    dependencyOverrides ++= Seq(
      "io.kamon" %% "kamon-core" % "1.0.0") 
  ) ++
  // skip api doc generation if SKIP_DOC env variable is defined
  Seq(sys.env.get("SKIP_DOC")).flatMap { _ =>
    Seq(
      publishArtifact in (Compile, packageDoc) := false,
      publishArtifact in packageDoc := false,
      sources in (Compile, doc) := Seq.empty
    )
  }
  
  lazy val testSettings = Seq(
    Test / fork := true,
    Test / parallelExecution := false,
    Test / testForkedParallel := false,
    fork in Test := false,
    parallelExecution in Test := false
  )

  lazy val itSettings = Defaults.itSettings ++ Seq(
    IntegrationTest / fork := true,
    parallelExecution in IntegrationTest := false,
    IntegrationTest / parallelExecution := false,
    IntegrationTest / testForkedParallel := false,
   scalaSource in IntegrationTest := baseDirectory.value / "src/it/scala",
   logBuffered in IntegrationTest := false,
    fork in IntegrationTest := true
  )

  lazy val compilerSettings = CompilerSettings.options ++ Seq(
    crossScalaVersions := Seq("2.11.12", scalaVersion.value)
)
  lazy val acq = Seq() ++ global ++ compilerSettings ++ testSettings  

  lazy val proxy = Seq() ++ global ++ compilerSettings ++ testSettings ++ itSettings

  lazy val core = Seq() ++ global ++ compilerSettings ++ testSettings  ++ itSettings

}
