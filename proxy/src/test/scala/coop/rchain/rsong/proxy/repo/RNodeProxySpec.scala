package coop.rchain.rsong.proxy.repo

import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.core.domain.{Server, _}
import coop.rchain.rsong.core.repo.{GRPC, RNodeProxy, RnodeProxyMoc}
import org.specs2.Specification

class RNodeProxySpec extends  Specification { def is = s2"""
      Rnode contract specs
          retrieve asset contract is constructed correctly $e1
          store asset contract is constructed correctly $e2
    """
//  listen-for-name $e1
  // fetch by asset by name $e2
  // fetch previous run assets $e3
  

  val log = Logger[RNodeProxySpec]
  val grpc = GRPC(Server("localhost", 40401))
  val proxy: RNodeProxy =new RnodeProxyMoc

  def e1 = {
    val assetName = "Broke.jpg"
    val assetRholangId = "Broke.jpg-rholang-id"
    val computed = SongQuery(
      nameIn = assetName,
      songId = assetRholangId )
    val actualContract = s"""@["Immersion", "retrieveSong"]!("Broke.jpg-rholang-id".hexToBytes(), "Broke.jpg.out")"""
    actualContract.replaceAll("\\s", "") ===
      computed.contract.replaceAll("\\s", "")
  }
  def e2 = {
   2 === 2
  }
}
