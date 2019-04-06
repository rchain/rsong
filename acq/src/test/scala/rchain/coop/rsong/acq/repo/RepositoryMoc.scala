package coop.rchain.rsong.acq.repo

import coop.rchain.rsong.acq.domain.Domain
import coop.rchain.rsong.core.domain.{Err, Query}

class RepositoryMoc extends Repository {
override def deployFile(
    path: String
  ): Either[Err, String] = Right(path)
override def deployAsset(
    asset: Domain.RawAsset
  ): Either[Err, String] = Right(asset.toString)
override def deployAsset(
    assets: Seq[Domain.RawAsset]
  ): Either[Err, String] = Right(s"${assets.size}")
  override def proposeBlock: Either[Err, String] = Right("Success")
override def retrieveToName(
    asset: Domain.RawAsset
  ): Either[Err, String] = Right(asset.id)
override def retrieveToName(
    assets: Seq[Domain.RawAsset]
  ): Either[Err, String] =Right(s"${assets.size}")
override def findDataAtName(name: String)(
    maxDepth: Int
  ): Either[Err, String] = Right("name")
override def setDataAtName(
    q: Query
  ): Either[Err, Unit] = Right(())
}
