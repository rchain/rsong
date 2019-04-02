package coop.rchain.rsong.proxy.repo

import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.core.domain.Err
import coop.rchain.rsong.core.repo.AssetRepo
import coop.rchain.rsong.proxy.domain.Domain._
import coop.rchain.rsong.core.utils.{Base16 => B16}

trait Repo {
  def queryAsset(name: String): Either[Err, String] 
  def queryBinaryAsset(name: String): Either[Err, Array[Byte]] =
    queryAsset(name) flatMap B16.decode
}

object Repo {
  def apply(assetRepo: AssetRepo): Repo = new Repo {
    val log = Logger[Repo.type ]
    def queryAsset(nameIn: String): Either[Err, String] = {
      for {
        n ← assetRepo.dataAtName(s""""$nameIn"""", Int.MaxValue) // get the Orig Contract
        q = SongQuery(nameIn, n)
       _=log.info(s"SongQuery for asset=$nameIn = ${q.contract}")
        s ← assetRepo.getAsset(q)

      }yield (s)
    }
 }
}
