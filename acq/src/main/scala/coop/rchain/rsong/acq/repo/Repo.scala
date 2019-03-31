package coop.rchain.rsong.acq.repo
import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.acq.domain.Domain.RawAsset
import coop.rchain.rsong.core.domain.{Err, RSongJsonAsset, Server}
import coop.rchain.rsong.core.repo.GRPC.GRPC
import coop.rchain.rsong.core.repo.{GRPC, RNodeProxy}
import coop.rchain.rsong.core.utils.FileUtil
import io.circe.generic.auto._
import io.circe.syntax._

trait Repo {
  def deployFile(path: String): Either[Err, String]
  def deployAsset(asset: RawAsset): Either[Err, String]
  def proposeBlock: Either[Err, String]
}
object Repo {
  val log = Logger[Repo.type ]

  def asRholang(asset: RSongJsonAsset) = {
    log.info(s"name to retrieve song: ${asset.id}")
    s"""@["Immersion", "store"]!(${asset.assetData}, ${asset.jsonData}, "${asset.id}")"""
  }


  def apply(grpc: GRPC, proxy: RNodeProxy): Repo = new Repo {
    def deployFile(path: String ): Either[Err, String] =

    for {
      c <- FileUtil.fileFromClasspath(path)
      d <- proxy.deploy(c)(grpc)
    } yield d

    def proposeBlock: Either[Err, String] = proxy.proposeBlock(grpc)

    import FileUtil._
    def deployAsset( asset: RawAsset ): Either[Err, String] =  {
    val asJsonAsset = RSongJsonAsset(id = asset.id,
      assetData = asHexConcatRsong(asset.uri).toOption.get,
      jsonData = asset.metadata.asJson.toString)
    (asRholang _ andThen proxy.deploy _) (asJsonAsset)(grpc)
  }
  }

}
