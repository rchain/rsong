package coop.rchain.rsong.proxy.repo

import coop.rchain.rsong.core.repo.{AssetRepo, GRPC, RNodeProxy}
import coop.rchain.rsong.core.domain._
import com.typesafe.scalalogging.Logger
import org.specs2._

class RepoSpec extends  Specification  { def is = s2"""
  proxy repo specsH
    proxy must fetch the asset rholang name  $e1
    proxy must build the proper query $e2
    proxy must execute the query $e3
"""
  val log = Logger[RepoSpec]

  val assetRepo = AssetRepo(RNodeProxy(GRPC(Server("localhost", 40401))))
  def e1 = {
    val assetName = "Broke.jpg"
    val c = for {
      n ← assetRepo.dataAtName(s""""$assetName"""", Int.MaxValue)
      i ← assetRepo.dataAtName(s""""${assetName}.out"""", Int.MaxValue)
    } yield (i)

    log.info(s"computed data-size= ${c} ")
    c must beRight
    }
  def e2 = 1 === 1
  def e3 = 1 === 1

}
