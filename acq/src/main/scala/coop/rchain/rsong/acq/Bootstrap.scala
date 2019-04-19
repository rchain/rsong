package coop.rchain.rsong.acq

import cats.effect._
import cats.syntax.all._
import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.acq.moc.MocSongMetadata
import coop.rchain.rsong.acq.service.AcqService
import coop.rchain.rsong.acq.utils.{Globals => G}
import coop.rchain.rsong.core.domain.{Err, RsongIngestedAsset, Server}
import coop.rchain.rsong.core.repo.RNodeProxyTypeAlias.{ConfigReader, EEString}
import coop.rchain.rsong.core.repo.{GRPC, RNodeProxy}

object Bootstrap extends IOApp {
  lazy val log = Logger[Bootstrap.type]
  val rnode = Server(G.rnodeHost, G.rnodePort)
  val grpc = GRPC(rnode)
  val proxy = RNodeProxy()
  val acq = AcqService(proxy)

  def run(args: List[String]): IO[ExitCode] =
    args.headOption match {
      case None =>
        IO(
          workAll(
            G.contractPath,
            MocSongMetadata.contents(G.rsongPath)
          ).run(grpc)
        ).as(ExitCode.Success)
          .handleError(e => {
            log.error(
              s"Contract Install/deploy failed with error: ${e.getMessage}"
            )
            ExitCode.Error
          })

      case Some(a) if a.equals("Deploy") =>
        IO(
          workContent(MocSongMetadata.contents(G.rsongPath))
            .run(grpc)
        ).as(ExitCode.Success)
          .handleError(e => {
            log.error(
              s"Deploy content has failed with error: ${e.getMessage}"
            )
            ExitCode.Error
          })

      case Some(a) if a.equals("Install") =>
        IO(installContract(G.contractPath).run(grpc))
          .as(ExitCode.Error)
          .handleError(e => {
            log.error(
              s"Contract Install failed with error: ${e.getMessage}"
            )
            ExitCode.Error
          })
    }
  def installContract(
      contractFile: String
  ): ConfigReader[EEString] =
    for {
      _ ← proxy.doDeployFile(contractFile)
      p ← proxy.doProposeBlock
    } yield (p)

  def workContent(
      contents: List[RsongIngestedAsset]
  ): ConfigReader[EEString] =
    (for {
      _ <- acq.storeBulk(contents)
      ids = contents.map(x => x.id)
      r ← acq.proposeBlock
      _ ← acq.prefetchBulk(ids)
      r ← acq.proposeBlock
    } yield r)

  def workAll(
      contract: String,
      contents: List[RsongIngestedAsset]
  ): ConfigReader[EEString] = {
    val ids = contents.map(x => x.id)
    (for {
      c <- installContract(contract)
      _ <- acq.storeBulk(contents)
      r ← acq.proposeBlock
      _ ← acq.prefetchBulk(ids)
      r ← acq.proposeBlock
    } yield r)
  }
}
