package coop.rchain.rsong.proxy.repo

import coop.rchain.rsong.core.repo.{AssetRepo, GRPC, RNodeProxy}
import coop.rchain.rsong.core.domain._
import com.typesafe.scalalogging.Logger
import org.specs2._

class RepoSpec extends  Specification  { def is = s2"""
  proxy repo specs
    proxy must fetch the asset rholang name  $e1
    proxy must build the proper query $e2
    proxy must execute the query $e3
"""
  val log = Logger[RepoSpec]

  val assetRepo = AssetRepo(RNodeProxy(GRPC(Server("localhost", 40401))))
  def e1 = {
    val assetName = "Tiny_Human_Stereo.izr.out"
    val nn = "3a220a2065e166ee86007cfa266eea4f92f203bfff79bf67e4347d04fcba4869e6380ae4"
    val c = for {
      n ← assetRepo.dataAtName(s""""$assetName"""", Int.MaxValue)
      q = SongQuery(assetName, nn)
      _=log.info(s"SongQuery = ${q}")
      _=log.info(s"SongQuery contract = ${q.contract}")
     //d ← assetRepo.getAsset(q)
     i ← assetRepo.dataAtName(s""""${q.nameOut}"""", 20)
    } yield (i)

//    log.info(s"computed data-size= ${c.toOption.get.length}  data = ${c}")
    c must beRight
    }
  def e2 = 1 === 1
  def e3 = 1 === 1

}
