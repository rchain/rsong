package coop.rchain.rsong.core.repo

import coop.rchain.casper.protocol.{DeployServiceGrpc, ProposeServiceGrpc}
import coop.rchain.rsong.core.domain.{Err, Server}
import io.grpc.ManagedChannelBuilder

object GRPC {
  type Propose = ProposeServiceGrpc.ProposeServiceBlockingStub
  type Deploy = DeployServiceGrpc.DeployServiceBlockingStub
val MAXGRPCSIZE = 1024 * 1024 * 1024 * 5 // 10G for a song+metadat
object Propose {

  def apply(s: Server): Propose = {
    val channel = ManagedChannelBuilder
      .forAddress(s.hostName, s.port)
      .maxInboundMessageSize(MAXGRPCSIZE)
      .usePlaintext
      .build
    ProposeServiceGrpc.blockingStub(channel)
  }
}

object Deploy {

  def apply(s: Server): Deploy = {
    val channel = ManagedChannelBuilder
      .forAddress(s.hostName, s.port)
      .maxInboundMessageSize(MAXGRPCSIZE)
      .usePlaintext
      .build
    DeployServiceGrpc.blockingStub(channel)
  }
}
}
