package coop.rchain.rsong.acq

import cats.effect._
import cats.syntax.all._
import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.acq.service.AcqService
import coop.rchain.rsong.acq.utils.{ Globals => G }
import coop.rchain.rsong.core.domain.{ Err, RsongIngestedAsset, Server }
import coop.rchain.rsong.core.repo.RNodeProxyTypeAlias.{ ConfigReader, EEString }
import coop.rchain.rsong.core.repo.{ GRPC, RNodeProxy }

object Bootstrap extends IOApp {
  lazy val log = Logger[Bootstrap.type]
  val rnode    = Server(G.rnodeHost, G.rnodePort)
  val grpc     = GRPC(rnode)
  val proxy    = RNodeProxy()
  val acq      = AcqService(proxy)

  def run(args: List[String]): IO[ExitCode] =
    args.headOption match {
      case Some(a) if a.equals("Install") =>
        IO {
          deployProposeContract(G.contractPath).run(grpc)
        }.as(ExitCode.Success)
          .handleError(e => {
            log.error(s"RsongAcquisition Install Contract has failed with error: ${e.getMessage}")
            ExitCode.Error
          })

      case Some(a) if a.equals("Deploy") =>
        IO {
          val z = for {
            _ <- deployProposeContract(G.contractPath)
            p <- deployPropseContents(Seq())
          } yield p
          z.run(grpc).head //TODO need to do a foldLeft AND COLLECT ERRORS
        }.as(ExitCode.Success)
          .handleError(e => {
            log.error(s"RsongAcquisition Install Contract has failed with error: ${e.getMessage}")
            ExitCode.Error
          })

      case None =>
        IO(System.err.println(s"Usage is ${args(0)} Install | Deploy")).as(ExitCode.Error)
    }

  def deployProposeContract(contractFile: String) =
    for {
      _ ← proxy.doDeployFile(contractFile)
      p ← proxy.doProposeBlock
    } yield (p)

  def deployContent(content: RsongIngestedAsset): ConfigReader[EEString] =
    for {
      c <- acq.store(content)
      p <- acq.prefetch(content.id)
    } yield p

  def deployPropseContents(contents: Seq[RsongIngestedAsset]): ConfigReader[Seq[EEString]] = ???

  def work(content: RsongIngestedAsset) =
    for {
      _ <- acq.store(content)
      a ← acq.prefetch(content.id)
      _ ← acq.proposeBlock
    } yield (a)

  def work2(content: RsongIngestedAsset) =
    for {
      _ <- acq.store(content)
      _ ← acq.prefetch(content.id)
      r ← acq.proposeBlock
    } yield (r)

}
