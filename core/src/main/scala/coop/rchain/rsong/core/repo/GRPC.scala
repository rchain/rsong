package coop.rchain.rsong.core.repo

import coop.rchain.casper.protocol.DeployServiceGrpc
import coop.rchain.rsong.core.domain.{ Err, Server }
import io.grpc.ManagedChannelBuilder

object GRPC {
  type GRPC = DeployServiceGrpc.DeployServiceBlockingStub

  val MAXGRPCSIZE = 1024 * 1024 * 1024 * 5 // 10G for a song+metadat
  def apply(s: Server): GRPC = {
    val channel = ManagedChannelBuilder
      .forAddress(s.hostName, s.port)
      .maxInboundMessageSize(MAXGRPCSIZE)
      .usePlaintext
      .build
    DeployServiceGrpc.blockingStub(channel)
  }
}

object ExecuteGRPC {
  import GRPC.GRPC
  def run(f: GRPC => Either[Err, String])(implicit grpc: GRPC) = f(grpc)
}
