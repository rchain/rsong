package coop.rchain.rsong.acq.service

import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.core.utils.Globals
import coop.rchain.rsong.acq.utils.{Globals => G}
import coop.rchain.rsong.core.repo.{GRPC, RNodeProxy}
import coop.rchain.rsong.core.domain.{RsongIngestedAsset, Server}

import org.specs2._

class AcqServiceSpec extends Specification {
  def is =
    s2"""
    Rsong AcqService Specs are:
       Deploy, propose, prefetch rsong assets $p1
  """

  lazy val log = Logger[AcqServiceSpec]
  val grpcPropose = GRPC.Propose( Server(G.rnodeHost, G.rnodePort) )
  val grpcDeploy = GRPC.Deploy( Server(G.rnodeHost, G.rnodePort) )
  val proxy = RNodeProxy()
  val acq = AcqService(proxy)

  val contractFile = Globals.appCfg.getString("contract.file.name")
  val maxTests= 3

  val contents = (1 to maxTests).map(
    x => RsongIngestedAsset(
      id=s"$x",
      data = s"content data for id=$x",
      metadata = s"content metadata for id=$x")
  ).toList
  val contenetIds = contents.map(x ⇒ s"${x.id}")

  def p1 = {

    /**
      * install contract
      **/
    val deployContract = acq.installContract(contractFile).run(grpcDeploy)
    log.info(s"deploy results from deploy contract: ${deployContract}")
    val proposeContract = acq.proposeBlock.run(grpcPropose)
    log.info(s"propose results from installContract: ${proposeContract}")
    proposeContract must beRight

    val deploys = acq.storeBulk(contents).run(grpcDeploy)
    log.debug(s"deploy results storeBulk: ${deploys}")
    val propose = acq.proposeBlock.run(grpcPropose)
    log.info(s"propose results from StoreBulk: ${deploys}")
    propose must beRight

     val prefetch = acq.prefetchBulk(contents.map(x => x.id))
       .run(grpcDeploy)
    log.info(s"deploys result from prefetch: ${prefetch}")
    val proposePrefetch = acq.proposeBlock.run(grpcPropose)
    log.debug(s"propose results from prefetch: ${proposePrefetch}")
    proposePrefetch must beRight
  }
}
