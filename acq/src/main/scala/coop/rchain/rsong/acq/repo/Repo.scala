package coop.rchain.rsong.acq.repo
import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.acq.domain.Domain.RawAsset
import coop.rchain.rsong.core.domain.{Err, RSongJsonAsset, Server}
import coop.rchain.rsong.core.repo.RNodeProxy
import coop.rchain.rsong.core.utils.FileUtil
import io.circe.generic.auto._
import io.circe.syntax._
import cats.implicits._
import cats.{Monoid}

trait Repo {
  def deployFile(path: String): Either[Err, String]
  def deployAsset(asset: RawAsset): Either[Err, String]
  def proposeBlock: Either[Err, String]
  def deployAsset(assets: Seq[RawAsset]): Either[Err, String] 
}
object Repo {
  type EIS = Either[Err, String]

  val log = Logger[Repo.type ]
  implicit val eisMonoid: Monoid[EIS] = new Monoid[EIS] {
    def empty: EIS = Right("")
    def combine(e1: EIS , e2: EIS) : EIS = {
      e1 match {
        case Left(e) ⇒ Left(e)
        case Right(r) ⇒ e2.map( x ⇒ r ++ "&" ++ x )
      }
    }
  }
  def asRholang(asset: RSongJsonAsset) = {
    log.info(s"name to retrieve song: ${asset.id}")
    s"""@["Immersion", "store"]!(${asset.assetData}, ${asset.jsonData}, "${asset.id}")"""
  }


  def apply(proxy: RNodeProxy): Repo = new Repo {
    def deployFile(path: String ): Either[Err, String] =

    for {
      c <- FileUtil.fileFromClasspath(path)
      d <- proxy.deploy(c)
    } yield d

    def proposeBlock: Either[Err, String] = proxy.proposeBlock

    import FileUtil._
    def deployAsset( asset: RawAsset ): Either[Err, String] =  {
    val asJsonAsset = RSongJsonAsset(id = asset.id,
      assetData = asHexConcatRsong(asset.uri).toOption.get,
      jsonData = asset.metadata.asJson.toString)
      (asRholang _ andThen proxy.deploy _) (asJsonAsset)
    }

  def deployAsset(assets: Seq[RawAsset]): Either[Err, String] = {
    (  assets map deployAsset )
      .foldLeft(eisMonoid.empty)(eisMonoid.combine(_, _))
  }
  }
}
