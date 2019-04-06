package coop.rchain.rsong.core.repo

import coop.rchain.rsong.core.domain.Err

class RnodeProxyMoc extends RNodeProxy {
override def deploy(
    contract: String
  ): Either[Err, String] = Right(contract)
override def proposeBlock
    : Either[Err, String] = Right("Success")
override def dataAtName(
    name: String
  ): Either[Err, String] = Right(name)
override def dataAtName(
    name: String,
    depth: Int
  ): Either[Err, String] = Right(s"{${name}${depth}")
}
