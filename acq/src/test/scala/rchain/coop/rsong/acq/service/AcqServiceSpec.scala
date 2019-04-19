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

class AcqServiceSpec extends Specification with ScalaCheck with BeforeEach {
  def is = s2"""
    AcqService specification are:
     0 bulk store and builk prefetch $p1
  """
  // 0 store and prefetch $p0

  val log = Logger[AcqServiceSpec]
  val contractFile = Globals.appCfg.getString("contract.file.name")
  val server = Server(hostName = "localhost", port = 40401)
  val grpc = GRPC(server)
  val proxy = RNodeProxy()
  val acq = AcqService(proxy)

  def p1 = {
    val contents: List[RsongIngestedAsset] = (0 to 20)
      .map(
        x ⇒
          RsongIngestedAsset(
            s"$x",
            s"${java.util.UUID.randomUUID}",
            s"${java.util.UUID.randomUUID.toString}"
          )
      )
      .toList
    val work = for {
      _ ← acq.storeBulk(contents)
      _ ← acq.proposeBlock
      ids = contents.map(_.id)
      b ← acq.prefetchBulk(ids)
      _ = log.info(s"prefetch bulk results are: ${b}")
      p ← acq.proposeBlock
    } yield (p)
    val computed: EEString = work.run(grpc)
    log.info(s"bulk deploy/propose results are: ${computed}")
    computed must beRight
  }

  val contentGen =
    for {
      id ← Gen.identifier
      data ← Gen.alphaStr
      metadata ← Gen.alphaStr
    } yield
      RsongIngestedAsset(
        id + java.util.UUID.randomUUID.toString,
        data + java.util.UUID.randomUUID.toString,
        metadata + java.util.UUID.randomUUID.toString
      )

  var v: Int = 0
  def before = {
    v = 0
    val work = for {
      _ ← acq.installContract(contractFile)
      p ← acq.proposeBlock
    } yield (p)
    work.run(grpc)
    log.info(s"contract is deployed & proposed.")
  }
  val p0: Prop = Prop.forAll(contentGen)(content ⇒ {
    v = v + 1
    val work: ConfigReader[EEString] = for {
      _ ← acq.store(content)
      _ ← acq.proposeBlock
      _ = log.info(
        s"*** counter v = ${v} **** prefetching contentid = ${content.id}"
      )
      _ ← acq.prefetch(content.id)
      p ← acq.proposeBlock
    } yield p
    val computed = work.run(grpc)
    log.info(s"---- computed val is = ${computed}")
    computed.isRight == true
  })
}
