package coop.rchain.rsong.acq.repo
import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.acq.domain.Domain.RawAsset
import coop.rchain.rsong.core.domain.{Err, RSongJsonAsset, SongQuery}
import coop.rchain.rsong.core.repo.{AssetRepo}
import coop.rchain.rsong.core.utils.FileUtil
import io.circe.generic.auto._
import io.circe.syntax._
import cats.Monoid

trait Repo {
  def deployFile(path: String): Either[Err, String]
  def deployAsset(asset: RawAsset): Either[Err, String]
  def deployAsset(assets: Seq[RawAsset]): Either[Err, String]
  def proposeBlock: Either[Err, String]
  def retrievalName(asset: RawAsset) : Either[Err, String]
  def retrievalName(assets: Seq[RawAsset]) : Either[Err, String]

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


  def apply(assetRepo: AssetRepo): Repo = new Repo {
    def deployFile(path: String ): Either[Err, String] =

    for {
      c <- FileUtil.fileFromClasspath(path)
      d <- assetRepo.deploy(c)
    } yield d

    def proposeBlock: Either[Err, String] = assetRepo.proposeBlock

    import FileUtil._

    def deployAsset( asset: RawAsset ): Either[Err, String] =  {
    val asJsonAsset = RSongJsonAsset(id = asset.id,
      assetData = asHexConcatRsong(asset.uri).toOption.get,
      jsonData = asset.metadata.asJson.toString)
      (asRholang _ andThen assetRepo.deploy _) (asJsonAsset)
    }

  def deployAsset(assets: Seq[RawAsset]): Either[Err, String] = {
    (  assets map deployAsset )
      .foldLeft(eisMonoid.empty)(eisMonoid.combine(_, _))
  }


    def retrievalName(assets: Seq[RawAsset]): Either[Err, String]= {
      val res = assets map retrievalName
      res.foldLeft(eisMonoid.empty)(eisMonoid.combine(_, _))
    }

    def retrievalName(asset: RawAsset) = {
      for {
        n ← assetRepo.dataAtName(s""""${asset.id}"""", Int.MaxValue) // get the Orig Contract
        q = SongQuery(asset.id, n)
        _=log.info(s"deploying query: ${q.nameIn} --- ${q.nameOut} --- ${q.contract}")
        d <- assetRepo.deploy(q.contract)
      }yield (d)
    }

  }
}
