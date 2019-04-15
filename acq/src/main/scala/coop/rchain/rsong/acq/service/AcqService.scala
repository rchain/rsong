package coop.rchain.rsong.acq.service

import coop.rchain.rsong.acq.domain.Domain.Asset
import coop.rchain.rsong.core.domain.{ Err, RholangContract, RsongIngestedAsset }
import AcqService._
import coop.rchain.crypto.{ PrivateKey, PublicKey }
import coop.rchain.crypto.codec.Base16
import coop.rchain.rsong.core.utils.Globals
import io.circe.generic.auto._
import io.circe.syntax._
import coop.rchain.rsong.core.utils.FileUtil
import coop.rchain.rsong.core.repo.{ RNodeProxy }
import cats.implicits._
import coop.rchain.rsong.acq.domain.SongQuery
import coop.rchain.rsong.core.repo.RNodeProxyTypeAlias.{ ConfigReader, EE }

trait AcqService {

  def installContract(path: String): ConfigReader[EE]

  def store(asset: RsongIngestedAsset): ConfigReader[EE]

  def storeBulk(assets: List[RsongIngestedAsset]): ConfigReader[List[EE]] =
    assets.traverse(store)

  def retrieveToName(name: String): ConfigReader[EE]

  def retrieveToNames(names: List[String]): ConfigReader[List[EE]] =
    names.traverse(retrieveToName)

  def getDataAtName(rholangName: String, maxDepth: Int): ConfigReader[EE]

  def getDataAtName(rholangName: String): ConfigReader[EE] =
    getDataAtName(rholangName, Int.MaxValue)

  def proposeBlock: ConfigReader[EE]
}

object AcqService {

  private final val publicKey =
    PublicKey(Base16.unsafeDecode(Globals.appCfg.getString("bond.key.public")))
  private final val privateKey =
    PrivateKey(Base16.unsafeDecode(Globals.appCfg.getString("bond.key.private")))

  implicit class RholangQueryOps(q: SongQuery) {
    def asRholang =
      RholangContract(code = q.contract, privateKey = privateKey, publicKey = publicKey)
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

  def installContract(contractPath: String): ConfigReader[EE] =
    for {
      _ ← proxy.doDeployFile(contractPath)
      p ← proposeBlock
    } yield p

  def store(asset: RsongIngestedAsset): ConfigReader[EE] =
    for {
      d ← proxy.doDeploy(asset.asRholang)
    } yield d

  def retrieveToName(assetId: String): ConfigReader[EE] =
    for {
      nIn ← proxy.doDataAtName(s"""$assetId""", Int.MaxValue)
      q = SongQuery(assetId, nIn.toOption.get) //todo may need a monad transformer
      c = q.asRholang
      nOut ← proxy.doDeploy(c)
    } yield nOut

  def getDataAtName(rholangName: String, maxDepth: Int): ConfigReader[EE] =
    proxy.doDataAtName(rholangName)

  def proposeBlock: ConfigReader[EE] = proxy.doProposeBlock

}
