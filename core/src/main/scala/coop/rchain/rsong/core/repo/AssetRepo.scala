package coop.rchain.rsong.core.repo

import coop.rchain.rsong.core.domain.{Err, Query}
import coop.rchain.rsong.core.utils.{Base16 => B16}
import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.core.repo.GRPC.GRPC

trait AssetRepo {
//  def getAsset(assetName: String): Either[Err, Array[Byte]]
  def getAsset(q: Query): Either[Err, String]
}

object AssetRepo {
  lazy val log = Logger[AssetRepo.type]

  def apply(grpc: GRPC, proxy: RNodeProxy): AssetRepo =
    new AssetRepo {

      def getAsset(q: Query): Either[Err, String] = 
      for {
        _ ← proxy.deploy(q.contract)(grpc)
        _ ← proxy.proposeBlock(grpc)
        asStr ← proxy.dataAtName(q.nameOut)(grpc)
      } yield (asStr)

    }
}
