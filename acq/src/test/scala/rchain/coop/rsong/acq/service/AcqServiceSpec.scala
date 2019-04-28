package coop.rchain.rsong.acq.service

import cats.data.State
import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.core.domain._
import coop.rchain.rsong.acq.moc.MocSongMetadata
import coop.rchain.rsong.core.utils.Globals
import coop.rchain.rsong.core.repo.{GRPC, RNodeProxy}
import coop.rchain.rsong.core.repo.RNodeProxyTypeAlias.{ConfigReader, EEString}
import org.specs2._
import org.specs2.specification.BeforeEach
import org.specs2.scalacheck.Parameters
import org.scalacheck._
import org.scalacheck.Gen
import org.scalacheck.Gen.{alphaChar, listOfN, posNum}
import org.scalacheck.Prop.forAll

class AcqServiceSpec extends Specification {
  def is =
    s2"""
    AcqService is the content Acquisition compoenent of Rsong. This service Specs are:
       Deploy Contract, Bulk deploy/propose followed by Prefetch operations of contents
  """

  val log = Logger[AcqServiceSpec]
  val contractFile = Globals.appCfg.getString("contract.file.name")
  val server = Server(hostName = "localhost", port = 40401)
  val grpc = GRPC(server)
  val proxy = RNodeProxy()
  val acq = AcqService(proxy)

  val contentList = (100 to 135).map(
    x => RsongIngestedAsset(
      id=s"$x",
      data = s"content data for id=$x",
      metadata = s"content metadata for id=$x")
  ).toList
  val p1 = {
    import com.typesafe.config.ConfigResolver
    val work: ConfigReader[Int] = for {
      _ ← acq.installContract(contractFile)
      _ ← acq.proposeBlock
      s0 ← acq.storeBulk(contentList)
      _=log.info(s"storeBullk result = $s0")
      s1 ← acq.proposeBlock
      _=log.info(s"propose results of $s0 = $s1")
      s2 ← acq.prefetchBulk(contentList.map(x => s"${x.id}"))
      r0= s2.count( x ⇒ x.isLeft )
      _=log.info(s"prrefetch results of = $s2")
      _=log.warn(s"prefetech errors = ${r0}")
      s3 ← acq.proposeBlock
    } yield(r0)
    val computed = work.run(grpc)
    computed === 0
  }
}
