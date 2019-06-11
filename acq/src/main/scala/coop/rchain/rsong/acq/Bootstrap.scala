package coop.rchain.rsong.acq

import cats.effect._
import cats.syntax.all._
import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.acq.moc.MocSongMetadata
import coop.rchain.rsong.acq.service.AcqService
import coop.rchain.rsong.acq.utils.{Globals => G}
import coop.rchain.rsong.core.domain.{Err, RsongIngestedAsset, Server}
import coop.rchain.rsong.core.repo.{GRPC, RNodeProxy}

object Bootstrap extends IOApp {
  lazy val log = Logger[Bootstrap.type]
  val grpcPropose = GRPC.Propose( Server(G.rnodeHost, G.rnodePort) )
  val grpcDeploy = GRPC.Deploy( Server(G.rnodeHost, G.rnodePort) )
  val proxy = RNodeProxy()
  val acq = AcqService(proxy)

  def run(args: List[String]): IO[ExitCode] =
    args.headOption match {
      case None =>
        IO(
          workAll(
            G.contractPath,
            MocSongMetadata.contents(G.rsongPath)
          )
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
        ).as(ExitCode.Success)
          .handleError(e => {
            log.error(
              s"Deploy content has failed with error: ${e.getMessage}"
            )
            ExitCode.Error
          })

      case Some(a) if a.equals("Install") =>
        IO(
          installContract(G.contractPath)
        ).as(ExitCode.Success)
          .handleError(e => {
            log.error(
              s"Contract Install failed with error: ${e.getMessage}"
            )
            ExitCode.Error
          })
    }

  def installContract(
      contractFile: String
  ) = {
    val deploys = acq.installContract(contractFile).run(grpcDeploy)
    log.debug(s"deploy results from deploy contract: ${deploys}")
    val propose = acq.proposeBlock.run(grpcPropose)
    log.debug(s"propose results from installContract: ${propose}")
  }

  def workContent(
      contents: List[RsongIngestedAsset]
  ) : Unit= {
    val deploys =  acq.storeBulk(contents).run(grpcDeploy)
    log.debug(s"deploy results storeBulk: ${deploys}")
    val proposeContent = acq.proposeBlock.run(grpcPropose)
    log.debug(s"propose results from StoreBulk: ${proposeContent}")
    val putAtNames = acq.prefetchBulk(contents.map(x => x.id))
    log.debug(s"deploys result from prefetch: ${putAtNames}")
    val propose = acq.proposeBlock.run(grpcPropose)
    log.debug(s"propose results from prefetch: ${propose}")
  }

  def workAll(
      contractFile: String,
      contents: List[RsongIngestedAsset]
  ):Unit = {
    installContract(contractFile)
    workContent(contents)
  }
}
