import sbt._
import Keys._


object Settings {
  lazy val global = Seq(
    organization := "coop.rchain.rsong",
    version := "1.0.0-SNAPSHOT" + sys.props.getOrElse("buildNumber", default="0-SNAPSHOT"),
    scalaVersion := "2.12.8",
    publishMavenStyle := true,
    publishArtifact in Test := false,
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots"),
      "jitpack" at "https://jitpack.io"
    ),
    testOptions in Test += Tests.Argument("-oD"), 
    dependencyOverrides ++= Seq(
      "io.kamon" %% "kamon-core" % "1.0.0"
    ),
    Test / fork := true,
    Test / parallelExecution := false,
    Test / testForkedParallel := false,
    IntegrationTest / fork := true,
    IntegrationTest / parallelExecution := false,
    IntegrationTest / testForkedParallel := false,
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
    fork in Test := false,
    parallelExecution in Test := false
  )

  lazy val itSettings = Defaults.itSettings ++ Seq(
    logBuffered in IntegrationTest := false,
    fork in IntegrationTest := true,
    scalaSource in IntegrationTest := baseDirectory.value / "src/it/scala")

  lazy val compilerSettings = CompilerSettings.options ++ Seq(
    crossScalaVersions := Seq("2.11.12", scalaVersion.value)
)
  lazy val acq = Seq() ++ global ++ compilerSettings ++ testSettings 

  lazy val proxy = Seq() ++ global ++ compilerSettings ++ testSettings

  lazy val core = Seq() ++ global ++ compilerSettings ++ testSettings 

}
