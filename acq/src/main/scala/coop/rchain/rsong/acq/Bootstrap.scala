package coop.rchain.rsong.acq

import cats.effect._
import cats.syntax.all._
import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.acq.domain.Domain.RawAsset
import coop.rchain.rsong.acq.moc.MocSongMetadata
import coop.rchain.rsong.acq.repo.Repo
import coop.rchain.rsong.acq.utils.{Globals => G}
import coop.rchain.rsong.core.domain.{Err, Server, SongQuery}
import coop.rchain.rsong.core.repo.{AssetRepo, GRPC, RNodeProxy}

object Bootstrap extends IOApp {
  lazy val log = Logger[Bootstrap.type]
  val rnode = Server(G.rnodeHost, G.rnodePort)
  val grpc = GRPC(rnode)
  val proxy = RNodeProxy(grpc)
  val assetRepo=AssetRepo(proxy)

  val repo = Repo(assetRepo)

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
          _ ← installContract(G.contractPath)
          a ← installAssets(G.rsongPath)
        r <- prefetch(a)
        } yield (a)
        IO(r).as(ExitCode.Success)
          .handleError(e => {
          log.error(s"RsongAcquisition has failed with error: ${e.getMessage}")
          ExitCode.Error
        })
    }

  def installContract(contractFile: String) = {
      for {
        _ ← repo.deployFile(contractFile)
        propose ← repo.proposeBlock
      } yield (propose)
  }

  def installAssets(path: String): Either[Err, Seq[RawAsset]] =
    (MocSongMetadata.assets _ andThen installAssets _) (path)

  def installAssets(assets: Seq[RawAsset]): Either[Err, Seq[RawAsset]] = {
    val installed = for {
      _ ← repo.deployAsset(assets)
      p ← repo.proposeBlock
    } yield (p)
   installed.map(_ ⇒ assets)
  }

  def prefetch(assets: Seq[RawAsset]) =
    for {
      r ← repo.retrieveToName(assets)
    _ ← repo.proposeBlock
   } yield (r)


}
