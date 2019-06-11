package coop.rchain.rsong.acq.service

import coop.rchain.rsong.core.domain.{Err, RholangContract, RsongIngestedAsset}
import AcqService._
import cats.data.EitherT
import coop.rchain.rsong.core.utils.{Base16 => B16}
import coop.rchain.rsong.core.repo.RNodeProxy
import cats.implicits._
import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.acq.domain.SongQuery
import coop.rchain.rsong.core.repo.RNodeProxyTypeAlias.{DeployReader, EEBin, EEString, ProposeReader}

trait AcqService {

  def installContract(path: String): DeployReader[EEString]

  def store(
      asset: RsongIngestedAsset
  ): DeployReader[EEString]

  def storeBulk(
      assets: List[RsongIngestedAsset]
  ): DeployReader[List[EEString]] =
    assets.traverse(store)

  def prefetch(name: String): DeployReader[EEString]

  def prefetchBulk(
      names: List[String]
  ): DeployReader[List[EEString]] =
    names.traverse(prefetch)

  def getDataAtName(
      rholangName: String,
      maxDepth: Int
  ): DeployReader[EEString]

  def getDataAtName(
      rholangName: String
  ): DeployReader[EEString] =
    getDataAtName(rholangName, Int.MaxValue)

  def getBinDataAtName(
      rholangName: String,
      maxDepth: Int
  ): DeployReader[EEBin]

  def getBinDataAtName(
      rholangName: String
  ): DeployReader[EEBin] =
    getBinDataAtName(rholangName, maxDepth = Int.MaxValue)

  def proposeBlock: ProposeReader[EEString]
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
  ): DeployReader[EEString] =
      proxy.doDeployFile(contractPath)

  def store(asset: RsongIngestedAsset): DeployReader[EEString] =
    proxy.doDeploy(asset.asRholangCode)

  def prefetch(assetId: String): DeployReader[EEString] = {
    log.info(s"in preftech, prefetching assetid=${assetId}")
    val nameOut: EitherT[DeployReader, Err, String] =
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
  ): DeployReader[EEBin] =
    proxy.doBinDataAtName(rholangName)

  def getDataAtName(
      rholangName: String,
      maxDepth: Int
  ): DeployReader[EEString] =
    proxy.doDataAtName(rholangName)

  def proposeBlock: ProposeReader[EEString] = {
    proxy.doProposeBlock
  }

}
