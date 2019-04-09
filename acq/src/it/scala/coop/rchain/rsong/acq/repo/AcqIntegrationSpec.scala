package coop.rchain.rsong.acq

import coop.rchain.rsong.core.repo._
import coop.rchain.rsong.core.domain._
import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.acq.domain.Domain.RsongAsset
import coop.rchain.rsong.acq.repo.Repository
import org.specs2._

class AcqIntegrationSpec extends Specification {
  def is    = s2"""
  acq integrationTest tests
    proxy must install rholang contract  $e0
    proxy must store asset rholang name  $e1
"""
  val log   = Logger[AcqIntegrationSpec]
  val rnode = Server("localhost", 40401)
  val grpc  = GRPC(rnode)
  val proxy = RNodeProxy(grpc)
  val repo  = Repository(proxy)
  val song: Song = Song(
    id = "song-123",
    title = "title-123",
    name = "name-123",
    audio = Nil,
    language = "EN"
  )
  val metadata = SongMetadata(
    song = song,
    artists = Nil,
    artwork = Nil,
    album = None
  )
  val rawAsset = RsongAsset(id = "asset-id-123", uri = "/moc-asset.jpg", metadata = metadata)

  def e0 = {
    val contract = "/rho/rsong-immersion.rho"
    val computed = repo.deployFile(contract)
    log.info(s"deployed contract result: ${computed}")
    computed must beRight
  }
  def e1 = {
    val asset    = "/moc-asset.jpg"
    val computed = repo.deployAsset(rawAsset)
    log.info(s"deployed Assets result: ${computed}")
    computed must beRight

  }
}
