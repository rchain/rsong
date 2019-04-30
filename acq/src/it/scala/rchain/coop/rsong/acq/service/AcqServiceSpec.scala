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
       Deploy Contract, Bulk deploy/propose followed by Prefetch operations for content count of 30 t$p1
  """

  val log = Logger[AcqServiceSpec]
  val contractFile = Globals.appCfg.getString("contract.file.name")
  val server = Server(hostName = "localhost", port = 40401)
  val grpc = GRPC(server)
  val proxy = RNodeProxy()
  val acq = AcqService(proxy)
  val maxTests = 5 FFFS S SA AS AS DFASD

  val contentList = (1 to maxTests).map(
    x => RsongIngestedAsset(
      id=s"$x",
      data = s"content data for id=$x",
      metadata = s"content metadata for id=$x")
  ).toList
  val contenetIds = contentList.map(x ⇒ s"${x.id}")

  val p1 = {
    import com.typesafe.config.ConfigResolver
    val work: ConfigReader[(Int, Int)] = for {
      _ ← acq.installContract(contractFile)
      _ ← acq.proposeBlock
      s0 ← acq.storeBulk(contentList)
      _=log.info(s"storeBullk total number of tests: $maxTests. Successfull stores: = ${s0.count(_.isRight)}")
      s1 ← acq.proposeBlock
      s2 ← acq.prefetchBulk(contentList.map(x => s"${x.id}"))
      (l,r)= (s2.count(_.isLeft), s2.count(_.isRight))
      s3 ← acq.proposeBlock
    } yield((l,r))
    val computed = work.run(grpc)
    log.info(s"prrefetch results: failures: ${computed._1} success: ${computed._2} bulk-size: ${contenetIds.size}")
    computed._1 === 0
  }
}
