package coop.rchain.rsong.acq.service

import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.core.domain._
import coop.rchain.rsong.core.utils.Globals
import coop.rchain.rsong.core.repo.{GRPC, RNodeProxy}
import coop.rchain.rsong.core.repo.RNodeProxyTypeAlias.{ConfigReader, EEString}
import org.specs2._
import org.specs2.specification.BeforeEach
import org.scalacheck._
import org.scalacheck.Gen

class AcqServiceSpec extends Specification with ScalaCheck with BeforeEach {
  def is =
    s2"""
    AcqService specification are:
     1 bulk store, bulk prefetch $p0
  """

  //  0 store and prefetch $p0

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
    val ids = contents.map(_.id)
    val work = for {
      //      p0 ← acq.storeBulk(contents)
      //      _ = log.info(s"store bulk results are: ${p0}")
      //      p1 ← acq.proposeBlock
      //      _ = log.info(s"proposeBlock from store-assets- results are: ${p1}")
      p2 ← acq.prefetchBulk(ids)
      _ = log.info(s"prefetch bulk results are: ${p2}")
      p3 ← acq.proposeBlock
    } yield (p2)
    val computed: List[EEString] = work.run(grpc)
    log.info(s"bulk deploy/propose results are: ${computed}")
    log.info(s" errors: ${computed.count(x => x.isLeft)}")
    log.info(s" corrects: ${computed.count(x => x.isRight)}")
    computed.count(x => x.isLeft) === 0
  }

  val contentGen =
    for {
      id ← Gen.identifier
      data ← Gen.alphaStr
      metadata ← Gen.alphaStr
    } yield
      RsongIngestedAsset(
        id,
        data,
        metadata
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
      s0 ← acq.store(content)
      _ = log.info(
        s"counter v = ${v} stored content=${content} -- result=${s0}"
      )
      _ ← acq.proposeBlock
      _ = log.info(s"counter v = ${v} prefetching contentid = ${content.id}")
      s1 ← acq.prefetch(content.id)
      _ = log.info(
        s"counter v = ${v} prefetched contentid = ${content.id} result= ${s1}"
      )
      s2 ← acq.proposeBlock
    } yield s2
    val computed = work.run(grpc)
    log.info(s"---- computed val is = ${computed}")
    computed.isRight == true
  })
}
