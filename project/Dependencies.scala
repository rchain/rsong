import sbt._

object Dependencies {
  object V {
    val redisCache    = "0.24.3"
    val kamon         = "1.0.0"
    val http4s        = "0.19+"
    val spec2        = "4.5.1"
    val logback       = "1.2.3"
    val scala_logging = "3.9.0"
    val config        = "1.3.2"
    val scalapb       = "0.7.4"
    val circie        = "0.9.3"
    val catsEffect    = "1.2.0"
    val catsCore      = "1.6.0"
    val monix         = "3.0.0-RC1"
  }

  object library {
    val magnolia = "com.propensive" %% "magnolia" % "0.10.0"
    val spec2 = "org.specs2" %% "specs2-scalacheck" % V.spec2  % "it, test"
    val catsEffect = "org.typelevel" %% "cats-effect" % V.catsEffect
    val monix  = "io.monix" %% "monix" % V.monix
    val circie_core = "io.circe" %% "circe-core" % V.circie
    val circie_generic = "io.circe" %% "circe-generic" % V.circie
    val circie_parser = "io.circe" %% "circe-parser" % V.circie
    val config  = "com.typesafe" % "config" % V.config
    val scala_logging = "com.typesafe.scala-logging" %% "scala-logging" % V.scala_logging
    val scalapb_compiler = "com.thesamet.scalapb" %% "compilerplugin" % V.scalapb
    val scalapb_runtime  = "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
    val grpc_netty  = "io.grpc" % "grpc-netty" % scalapb.compiler.Version.grpcJavaVersion
    val scalapb_grpc = "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion
    val logback = "ch.qos.logback" % "logback-classic" % V.logback
    val redis = "com.github.cb372" %% "scalacache-redis" % V.redisCache
    val http4s_dsl = "org.http4s" %% "http4s-dsl" % V.http4s
    val http4s_blaze = "org.http4s" %% "http4s-blaze-server" % V.http4s
    val http4s_circie = "org.http4s" %% "http4s-circe" % V.http4s
    val kamon = "io.kamon" %% "kamon-prometheus" % V.kamon
    val bouncyCastle = "org.bouncycastle" % "bcprov-jdk15on" % "1.60"
    val kalium = "com.github.rchain" % "kalium" % "0.8.1"
    val secp256k1Java = "com.github.rchain" % "secp256k1-java" % "0.1"
  }

  object Dep {
    val acq: Seq[ModuleID] = Seq(
      library.magnolia,
      library.config,
      library.scalapb_grpc,
      library.grpc_netty,
      library.scala_logging,
      library.circie_core,
      library.circie_generic,
      library.circie_parser,
      library.scala_logging,
      library.logback,
      library.spec2
    )

    val proxy: Seq[ModuleID] = Seq(
      library.magnolia,
      library.config,
      library.scalapb_grpc,
      library.grpc_netty,
      library.scala_logging,
      library.circie_core,
      library.circie_generic,
      library.circie_parser,
      library.http4s_circie,
      library.http4s_blaze,
      library.http4s_dsl,
      library.redis,
      library.kamon,
      library.scala_logging,
      library.logback,
      library.spec2
    )

    val core: Seq[ModuleID] = Seq(
      library.magnolia,
      library.config,
      library.scalapb_grpc,
      library.grpc_netty,
      library.scala_logging,
      library.logback,
      library.monix,
      library.catsEffect,
      library.bouncyCastle,
      library.secp256k1Java,
      library.kalium,
      library.spec2
    )
  }
}
