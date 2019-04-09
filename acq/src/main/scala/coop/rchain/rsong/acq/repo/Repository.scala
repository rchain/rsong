package coop.rchain.rsong.acq.repo
import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.acq.domain.Domain.RsongAsset
import coop.rchain.rsong.core.domain._
import coop.rchain.rsong.core.repo.RNodeProxy
import coop.rchain.rsong.core.utils.FileUtil
import io.circe.generic.auto._
import io.circe.syntax._
import cats.Monoid

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

object Repository {

  type EIS = Either[Err, String]

  val log = Logger[Repository.type]
  implicit val eisMonoid: Monoid[EIS] = new Monoid[EIS] {
    def empty: EIS = Right("")
    def combine(e1: EIS, e2: EIS): EIS =
      e1 match {
        case Left(e)  ⇒ Left(e)
        case Right(r) ⇒ e2.map(x ⇒ r ++ "&" ++ x)
      }
  }
  def asRholang(asset: RSongJsonAsset) = {
    log.info(s"name to retrieve song: ${asset.id}")
    s"""@["Immersion", "store"]!(${asset.assetData}, ${asset.jsonData}, "${asset.id}")"""
  }

  def apply(proxy: RNodeProxy): Repository = new Repository {
    def deployFile(path: String): Either[Err, String] =
      for {
        c <- FileUtil.fileFromClasspath(path)
        d <- proxy.deploy(c)
      } yield d

    def proposeBlock: Either[Err, String] = proxy.proposeBlock

    import FileUtil._

    def deployAsset(asset: RsongAsset): Either[Err, String] = {
      val asJsonAsset = RSongJsonAsset(id = asset.id,
                                       assetData = asHexConcatRsong(asset.uri).toOption.get,
                                       jsonData = asset.metadata.asJson.toString)
      (asRholang _ andThen proxy.deploy _)(asJsonAsset)
    }

    def deployAsset(assets: Seq[RsongAsset]): Either[Err, String] =
      (assets map deployAsset)
        .foldLeft(eisMonoid.empty)(eisMonoid.combine(_, _))

    def retrieveToName(assets: Seq[RsongAsset]): Either[Err, String] = {
      val res = assets map retrieveToName
      res.foldLeft(eisMonoid.empty)(eisMonoid.combine(_, _))
    }

    def retrieveToName(asset: RsongAsset) =
      for {
        n ← proxy.dataAtName(s""""${asset.id}"""", Int.MaxValue) // get the Orig Contract
        q = SongQuery(asset.id, n)
        _ = log.info(s"deploying query: ${q.nameIn} --- ${q.nameOut} --- ${q.contract}")
        d <- proxy.deploy(q.contract)
      } yield (d)

    def findDataAtName(name: String)(maxDepth: Int): Either[Err, String] = {
      def helper(depth: Int): Either[Err, String] = {
        println(s"___DEPTH = $depth.  name= $name ---")
        proxy.dataAtName(name, depth) match {
          case Left(Err(OpCode.nameNotFound, _)) if depth < maxDepth => helper(depth + 1)
          case Right(r)                                              => Right(r)
          case Left(e)                                               => Left(e)
        }
      }
      helper(0)
    }

    def setDataAtName(q: Query): Either[Err, Unit] =
      for {
        _ ← proxy.deploy(q.contract)
        _ ← proxy.proposeBlock
        _ = log.info(s"contract → ${q.contract} was deployed and proposed.")
      } yield ()
  }
}
