package coop.rchain.rsong.core.repo

import coop.rchain.crypto.{ PrivateKey, PublicKey }
import coop.rchain.crypto.codec.Base16
import coop.rchain.rsong.core.domain._
import org.specs2._
import org.specs2.scalacheck.Parameters

//deploy contract $e1
class RNodeProxySpec extends Specification {
  def is = s2"""
  RNodeProsy specs are
       deploy/propose a list of contracts $e1
"""
  sequential
  implicit val params = Parameters(minTestsOk = 20)

  val rholangcode: Int ⇒ String = i ⇒ s"""
      @["Immersion", "${i}-retrieveSong"]!(
"3a220a209ad777c23f1f88e6794710bfd790789ed3fed0c8e34108b2fb2a43b722ec4dfb".hexToBytes(),
"Broke.jpg.out") """

  val privateKey = PrivateKey(Base16.unsafeDecode("6bd8981cf922a547ca3c2d218f747d8048b7999a2f744f14e124a7082991b7e3"))
  val pubKey     = PublicKey(Base16.unsafeDecode("5b1aaf6d8d99677749bf32a0257401fe072d62a749799a5ef7b47306ea73d663"))
  val server     = Server(hostName = "localhost", port = 40401)

  val grpc  = GRPC(server)
  val proxy = RNodeProxy()

  def e1 = {
    val contracts: List[RholangContract] =
      (1 to 20).map(i => (RholangContract(code = rholangcode(i), privateKey = privateKey, publicKey = pubKey))).toList

    val computed = for {
      ds ← proxy.doDeploys(contracts)
      p  ← proxy.doProposeBlock
    } yield p
    val res = computed.run(grpc)
    res must beRight
  }
}
