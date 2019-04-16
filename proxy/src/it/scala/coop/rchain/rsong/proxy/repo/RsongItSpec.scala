package coop.rchain.rsong.proxy.repo

import coop.rchain.crypto.{ PrivateKey, PublicKey }
import coop.rchain.crypto.codec.Base16
import coop.rchain.crypto.signatures.Ed25519
import coop.rchain.rsong.acq.moc.MocSongMetadata
import coop.rchain.rsong.acq.service.AcqService
import coop.rchain.rsong.core.domain._
import coop.rchain.rsong.core.repo._
import coop.rchain.rsong.core.utils.{ FileUtil, Globals }
import coop.rchain.rsong.acq.service.AcqService._
import org.specs2._
import org.specs2.scalacheck.Parameters

//deploy contract $e1
class RsongItSpec extends Specification {
  def is = s2"""
  RNodeProsy specs are
       deploy/propose a list of contracts $e2
"""
  sequential
  implicit val params = Parameters(minTestsOk = 20)

  val rholangcode: Int ⇒ String = i ⇒ s"""
      @["Immersion", "${i}-retrieveSong"]!(
"3a220a209ad777c23f1f88e6794710bfd790789ed3fed0c8e34108b2fb2a43b722ec4dfb".hexToBytes(),
"Broke.jpg.out") """

//  val publicKey  = PublicKey(Base16.unsafeDecode(Globals.appCfg.getString("bond.key.public")))
//  val privateKey = PrivateKey(Base16.unsafeDecode(Globals.appCfg.getString("bond.key.private")))
  val (prK, puK) = Ed25519.newKeyPair

  val server = Server(hostName = "localhost", port = 40401)

  val grpc  = GRPC(server)
  val proxy = RNodeProxy()
  val acq   = AcqService(proxy)

  def installContract(contractFile: String) =
    for {
      _ ← proxy.doDeployFile(contractFile)
      p ← proxy.doProposeBlock
    } yield (p)

  def e1 = {
    val contracts: List[RholangContract] =
      (1 to 20)
        .map(i => (RholangContract(code = rholangcode(i), privateKey = prK, publicKey = puK)))
        .toList

    val computed = for {
      ds ← proxy.doDeploys(contracts)
      p  ← proxy.doProposeBlock
    } yield p
    val res = computed.run(grpc)
    res must beRight
  }
  def e2 = {
    val acq             = AcqService(proxy)
    val contractPath    = Globals.appCfg.getString("contract.file.name")
    val bytes           = "011001100110111101101111"
    val ingestedContent = RsongIngestedAsset("Broke.jpg", FileUtil.logDepth(bytes), FileUtil.logDepth("metdata"))

    val work = for {
      // _ ← installContract(contractPath)
      // _ ← acq.store(ingestedContent)
      _ ← acq.prefetch(ingestedContent.id)
      _ ← acq.proposeBlock
      n ← acq.getDataAtName(s"${ingestedContent.id}.out")
    } yield n

    val computed = work.run(grpc)

    println(s"----------0000 =++ fetched name = ${computed}")
    computed must beRight
  }
}
