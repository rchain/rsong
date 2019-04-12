package coop.rchain.rsong.acq.repo

import coop.rchain.rsong.acq.domain.Domain.RsongAsset
import coop.rchain.rsong.acq.domain.Query
import coop.rchain.rsong.core.domain.Err

trait Repository {
  def deployFile(path: String): Either[Err, String]
  def deployAsset(asset: RsongAsset): Either[Err, String]
  def deployAsset(assets: Seq[RsongAsset]): Either[Err, String]
  def proposeBlock: Either[Err, String]
  def retrieveToName(asset: RsongAsset): Either[Err, String]
  def retrieveToName(assets: Seq[RsongAsset]): Either[Err, String]
  def findDataAtName(name: String)(maxDepth: Int): Either[Err, String]
  def setDataAtName(q: Query): Either[Err, Unit]
  def getAsset(q: Query): Either[Err, String] =
    for {
      _     ← setDataAtName(q)
      asStr ← findDataAtName(s""""${q.nameOut}"""")(20)
    } yield (asStr)
}
