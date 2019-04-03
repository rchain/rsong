package coop.rchain.rsong.proxy.repo

import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.core.domain.{Err, SongQuery}
import coop.rchain.rsong.core.repo.AssetRepo
import coop.rchain.rsong.core.utils.{Base16 => B16}

trait Repo {
  def queryAsset(name: String): Either[Err, String] 
  def queryBinaryAsset(name: String): Either[Err, Array[Byte]] =
    queryAsset(name) flatMap B16.decode
}

object Repo {
  def apply(assetRepo: AssetRepo): Repo = new Repo {
    val log = Logger[Repo.type ]
    def queryAsset(nameIn: String): Either[Err, String] =
        assetRepo.dataAtName (s""""${nameIn}.out"""", Int.MaxValue)
 }
}
