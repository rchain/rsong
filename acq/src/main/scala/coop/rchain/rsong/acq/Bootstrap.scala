package coop.rchain.rsong.acq

import cats.effect._
import cats.syntax.all._
import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.acq.domain.Domain.RawAsset
import coop.rchain.rsong.acq.moc.MocSongMetadata._
import coop.rchain.rsong.acq.repo.Repo
import coop.rchain.rsong.acq.utils.{Globals => G}
import coop.rchain.rsong.core.domain.Server
import coop.rchain.rsong.core.repo.{GRPC, RNodeProxy}

object Bootstrap extends IOApp {
  lazy val log = Logger[Bootstrap.type]
  val rnode = Server(G.rnodeHost, G.rnodePort)
  val grpc = GRPC(rnode)
  val nodeProxy = RNodeProxy()

  val repo = Repo(grpc, nodeProxy)

  def run(args: List[String]): IO[ExitCode] =
    args.headOption match {
      case Some(a) if a.equals("Install") =>
        IO(installContract(G.contractPath)) .as(ExitCode.Success)
          .handleError(e => {
            log.error(s"RsongAcquisition Install Contract has failed with error: ${e.getMessage}")
            ExitCode.Error
          })

      case Some(a) if a.equals("Deploy") =>
        IO(installAssets(G.rsongPath)).as(ExitCode.Success)
          .handleError(e => {
            log.error(s"RsongAcquisition Install Assets has failed with error: ${e.getMessage}")
            ExitCode.Error
          })

      case None =>
        val r = for {
          _ <- installContract(G.contractPath)
          a <- installAssets(G.rsongPath)
        } yield (a)
        IO(r).as(ExitCode.Success)
          .handleError(e => {
          log.error(s"RsongAcquisition has failed with error: ${e.getMessage}")
          ExitCode.Error
        })
    }

  def installContract(contractFile: String) = {
      for {
        _ <- repo.deployFile(contractFile)
        propose <- repo.proposeBlock
      } yield (propose)
  }

  def installAssets(path: String) = {
    for {
      _ ← repo.deployAsset(RawAsset(
                             "Broke.jpg",
                             s"$path/Labels/Broke.jpg",
                             mocSongs("Broke")))
/**
      _ ← repo.deployAsset(RawAsset(
                                 "Broke_Immersive.izr",
                                 s"$path/Songs/Broke_Immersive.izr",
                                 mocSongs("Broke")))

      _ ← repo.deployAsset(RawAsset(
                                  "Broke_Stereo.izr",
                                  s"$path/Songs/Broke_Stereo.izr",
                                  mocSongs("Broke")))

        _ ← repo.deployAsset(RawAsset(
                                "Euphoria_Immersive.izr",
                                s"$path/Songs/Euphoria_Immersive.izr",
                                mocSongs("Euphoria")))

        _ ← repo.deployAsset(RawAsset(
                               "Euphoria_Stereo.izr",
                               s"$path/Songs/Euphoria_Stereo.izr",
                               mocSongs("Euphoria")))

        _ ← repo.deployAsset(RawAsset(
                               "Euphoria.jpg",
                               s"$path/Labels/Euphoria.jpg",
                               mocSongs("Euphoria")))

        _ ← repo.deployAsset(RawAsset(
                                "Tiny_Human_Immersive.izr",
                                s"$path/Songs/Tiny_Human_Immersive.izr",
                                mocSongs("Tiny_Human")))
        _ ← repo.deployAsset(RawAsset(
                                "Tiny_Human_Stereo.izr",
                                s"$path/Songs/Tiny_Human_Stereo.izr",
                                mocSongs("Tiny_Human")))
        _ ← repo.deployAsset(RawAsset(
                                "Tiny Human.jpg",
                                s"$path/Labels/Tiny Human.jpg",
                                mocSongs("Tiny_Human")))
 * */
        propose <-repo.proposeBlock

      } yield (propose)
  }

}
