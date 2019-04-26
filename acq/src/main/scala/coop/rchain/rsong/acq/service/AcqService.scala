package coop.rchain.rsong.acq.service

import coop.rchain.rsong.core.domain.{Err, RholangContract, RsongIngestedAsset}
import AcqService._
import cats.data.EitherT
import coop.rchain.rsong.core.utils.{Base16 => B16}
import coop.rchain.rsong.core.repo.RNodeProxy
import cats.implicits._
import com.typesafe.scalalogging.Logger
import coop.rchain.crypto.signatures.Ed25519
import coop.rchain.rsong.acq.domain.SongQuery
import coop.rchain.rsong.core.repo.RNodeProxyTypeAlias.{
  ConfigReader,
  EEBin,
  EEString
}

trait AcqService {

  def installContract(path: String): ConfigReader[EEString]

  def store(
      asset: RsongIngestedAsset
  ): ConfigReader[EEString]

  def storeBulk(
      assets: List[RsongIngestedAsset]
  ): ConfigReader[List[EEString]] =
    assets.traverse(store)

  def prefetch(name: String): ConfigReader[EEString]

  def prefetchBulk(
      names: List[String]
  ): ConfigReader[List[EEString]] =
    names.traverse(prefetch)

  def getDataAtName(
      rholangName: String,
      maxDepth: Int
  ): ConfigReader[EEString]

  def getDataAtName(
      rholangName: String
  ): ConfigReader[EEString] =
    getDataAtName(rholangName, Int.MaxValue)

  def getBinDataAtName(
      rholangName: String,
      maxDepth: Int
  ): ConfigReader[EEBin]

  def getBinDataAtName(
      rholangName: String
  ): ConfigReader[EEBin] =
    getBinDataAtName(rholangName, maxDepth = Int.MaxValue)

  def proposeBlock: ConfigReader[EEString]
}

object AcqService {

  implicit class RholangQueryOps(q: SongQuery) {
    def asContract =
      RholangContract(code = q.contract)
  }

  implicit class ContentBinOps(data: String) {
    def asBin = B16.decode(data)
  }
  implicit class RholangAssetOps(
      asset: RsongIngestedAsset
  ) {
    def asRholangCode: String =
      s"""@["Immersion", "store"]!("${asset.data}", "${asset.metadata}", "${asset.id}")"""
  }

  def apply(proxy: RNodeProxy): AcqServiceImpl =
    new AcqServiceImpl(proxy)
}

class AcqServiceImpl(proxy: RNodeProxy) extends AcqService {

  val log = Logger[AcqService]
  def installContract(
      contractPath: String
  ): ConfigReader[EEString] =
    for {
      _ ← proxy.doDeployFile(contractPath)
      p ← proposeBlock
    } yield p

  def store(asset: RsongIngestedAsset): ConfigReader[EEString] =
    proxy.doDeploy(asset.asRholangCode)

  def prefetch(assetId: String): ConfigReader[EEString] = {
    val nameOut: EitherT[ConfigReader, Err, String] =
      for {
        nIn ← EitherT(proxy.doDataAtName(s"""$assetId"""))
        _ = log.info(
          s"for assetId = ${assetId} proxyDataAtName returned: ${nIn}"
        )
        q <- EitherT(proxy.lift(Right((SongQuery(assetId, nIn)).contract)))
        _ = log.info(s"for assetId = ${assetId} rholang-contract= ${q}")
        nOut <- EitherT(proxy.doDeploy(q))
      } yield q
    nameOut.value
  }

  def getBinDataAtName(
      rholangName: String,
      maxDepth: Int
  ): ConfigReader[EEBin] =
    proxy.doBinDataAtName(rholangName)

  def getDataAtName(
      rholangName: String,
      maxDepth: Int
  ): ConfigReader[EEString] =
    proxy.doDataAtName(rholangName)

  def proposeBlock: ConfigReader[EEString] =
    proxy.doProposeBlock

}
