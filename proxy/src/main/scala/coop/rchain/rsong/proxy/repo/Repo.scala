package coop.rchain.rsong.proxy.repo

import coop.rchain.rsong.core.domain.Err
import coop.rchain.rsong.core.repo.GRPC.GRPC
import coop.rchain.rsong.core.repo.AssetRepo
import coop.rchain.rsong.core.repo.RNodeProxy
import coop.rchain.rsong.proxy.domain.Domain._
import coop.rchain.rsong.core.utils.{Base16 => B16}

trait Repo {
  def queryAsset(name: String): Either[Err, String] 
  def queryBinaryAsset(name: String): Either[Err, Array[Byte]] =
    queryAsset(name) flatMap B16.decode
}

object Repo {
  def apply(grpc: GRPC, proxy: RNodeProxy): Repo = new Repo {
    val assetRepo = AssetRepo(grpc, proxy)
    def queryAsset(nameIn: String): Either[Err, String] = {
      for {
        n ← proxy.dataAtName(s""""$nameIn"""")(grpc)
        q = SongQuery(nameIn, n)
        s ← assetRepo.getAsset(q)
      }yield (s)
    }
 }
}
