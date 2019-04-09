package coop.rchain.rsong.proxy.repo

import coop.rchain.rsong.core.repo.{GRPC, RNodeProxy}
import coop.rchain.rsong.core.domain._
import com.typesafe.scalalogging.Logger
import org.specs2._

class ProxyIntegrationSpec extends  Specification  { def is = s2"""
  proxy integration specs
    proxy must fetch the asset rholang name  $e1
"""
  val log = Logger[ProxyIntegrationSpec]

  val assetRepo = RNodeProxy(GRPC(Server("localhost", 40401)))
  def e1 = {
    val assetName = "Broke.jpg"
    val c = for {
      n ← assetRepo.dataAtName(s""""$assetName"""", Int.MaxValue)
      i ← assetRepo.dataAtName(s""""${assetName}.out"""", Int.MaxValue)
    } yield (i)

    log.info(s"computed data-size= ${c} ")
    c must beRight
    }

}
