package coop.rchain.rsong.acq.service

import coop.rchain.rsong.acq.domain.Domain.Asset
import coop.rchain.rsong.core.domain.{ Err, RholangContract, RsongIngestedAsset }
import AcqService._
import coop.rchain.rsong.core.utils.{ Base16 => B16 }
import io.circe.generic.auto._
import io.circe.syntax._
import coop.rchain.rsong.core.utils.FileUtil
import coop.rchain.rsong.core.repo.RNodeProxy
import cats.implicits._
import coop.rchain.crypto.signatures.Ed25519
import coop.rchain.rsong.acq.domain.SongQuery
import coop.rchain.rsong.core.repo.RNodeProxyTypeAlias.{ ConfigReader, EEBin, EEString }

trait AcqService {

  def installContract(path: String): ConfigReader[EEString]

  def store(asset: RsongIngestedAsset): ConfigReader[EEString]

  def storeBulk(assets: List[RsongIngestedAsset]): ConfigReader[List[EEString]] =
    assets.traverse(store)

  def prefetch(name: String): ConfigReader[EEString]

  def prefetchBulk(names: List[String]): ConfigReader[List[EEString]] =
    names.traverse(prefetch)

  def getDataAtName(rholangName: String, maxDepth: Int): ConfigReader[EEString]

  def getDataAtName(rholangName: String): ConfigReader[EEString] =
    getDataAtName(rholangName, Int.MaxValue)

  def getBinDataAtName(rholangName: String, maxDepth: Int): ConfigReader[EEBin]

  def getBinDataAtName(rholangName: String): ConfigReader[EEBin] =
    getBinDataAtName(rholangName, maxDepth = Int.MaxValue)

  def proposeBlock: ConfigReader[EEString]
}

object AcqService {

  private final val (privateKey, publicKey) = Ed25519.newKeyPair

  implicit class RholangQueryOps(q: SongQuery) {
    def asRholang =
      RholangContract(code = q.contract, privateKey = privateKey, publicKey = publicKey)
  }

  implicit class ContentBinOps(data: String) {
    def asBin = B16.decode(data)
  }
  implicit class RholangAssetOps(asset: RsongIngestedAsset) {
    def asRholang: RholangContract = {
      val contractCode =
        s"""@["Immersion", "store"]!(${asset.data}, ${asset.metadata}, "${asset.id}")"""
      RholangContract(
        code = contractCode,
        privateKey = privateKey,
        publicKey = publicKey
      )
    }
  }

  implicit class RsongAssetOps(asset: Asset) {
    def asIngestedAsset: Either[Err, RsongIngestedAsset] =
      FileUtil
        .asHexConcatRsongFromFile(asset.uri)
        .map(x ⇒ RsongIngestedAsset(id = asset.id, data = x, metadata = asset.metadata.asJson.toString))
  }

  def apply(proxy: RNodeProxy): AcqServiceImpl =
    new AcqServiceImpl(proxy)
}

class AcqServiceImpl(proxy: RNodeProxy) extends AcqService {
  trait TX

  def installContract(contractPath: String): ConfigReader[EEString] =
    for {
      _ ← proxy.doDeployFile(contractPath)
      p ← proposeBlock
    } yield p

  def store(asset: RsongIngestedAsset): ConfigReader[EEString] =
    proxy.doDeploy(asset.asRholang)

  def prefetch(assetId: String): ConfigReader[EEString] =
    for {
      nIn  ← proxy.doDataAtName(s"""$assetId""")
      q    = SongQuery(assetId, nIn.toOption.get) //todo may need a monad transformer
      c    = q.asRholang
      nOut ← proxy.doDeploy(c)
    } yield nOut

  def getBinDataAtName(rholangName: String, maxDepth: Int): ConfigReader[EEBin] =
    proxy.doBinDataAtName(rholangName)

  def getDataAtName(rholangName: String, maxDepth: Int): ConfigReader[EEString] =
    proxy.doDataAtName(rholangName)

  def proposeBlock: ConfigReader[EEString] = proxy.doProposeBlock

}
