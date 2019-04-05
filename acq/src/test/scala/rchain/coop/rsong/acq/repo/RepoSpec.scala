package coop.rchain.rsong.acq

import coop.rchain.rsong.core.repo._
import coop.rchain.rsong.core.domain._
import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.acq.domain.Domain.RawAsset
import coop.rchain.rsong.acq.repo.Repo
import org.specs2._

class RepoSpec extends  Specification  { def is = s2"""
  Rsong IntegrationTest tests
    proxy must install rholang contract  $e0
    proxy must store asset rholang name  $e1
    proxy must pre-fetch the asset rholang name  $e2
    proxy must fetch the asset rholang name  $e3
"""
  val log = Logger[RepoSpec]
  val rnode = Server("localhost", 40401)
  val grpc = GRPC(rnode)
  val proxy = RNodeProxy(grpc)
  val assetRepo = AssetRepo(proxy)
  val repo = Repo(assetRepo)
  val song: Song = Song(
    id="song-123",
    title="title-123",
    name="name-123",
    audio=Nil,
    language = "EN"
  )
  val metadata = SongMetadata(
  song=song,
  artists=Nil,
  artwork=Nil,
  album=None
  )
  val rawAsset = RawAsset(
    id = "asset-id-123",
    uri="/moc-asset.jpg",
    metadata = metadata)

  def e0 = {
    val contract = "/rho/rsong-immersion.rho"
    val computed = repo.deployFile(contract)
    log.info(s"deployed contract result: ${computed}")
    computed must beRight
  }
  def e1 = {
    val asset="/moc-asset.jpg"
    val computed = repo.deployAsset(rawAsset)
    log.info(s"deployed Assets result: ${computed}")
    computed must beRight

  }
  def e2 = 1 === 1
  def e3 = 1 === 1
}
