/**
package coop.rchain.rsong.core.repo

import coop.rchain.either.{Either => CoopE}
import coop.rchain.rsong.core.domain.{Err, OpCode, Server}
import coop.rchain.rsong.core.repo.RNodeProxy._
import org.specs2._

class RholangProxySpec extends Specification { def is =
  s2"""
      RholangProxy Specs
        coop.rchain.either.Either to Scala Either conversion   $e1
        test the implict error conversions $e2
    """
  def e1 = {
    val computed: Either[Err, String] =
      CoopE.defaultInstance.asEither(OpCode.grpcDeploy)
    computed ===  Left(Err(
      OpCode.grpcDeploy,"No value was returned!"))
  }
  def e2 = {
    val contrct =
      """
        new chan1, stdout(`rho:io:stdout`) in {
          stdout!("I'm on the screen")
          |
          chan1!("I'm in the tuplespace")
        }
      """
    val rnode = RNodeProxy(Server("localhost",40401))
    val computed  = for {
      d <- rnode.deploy(contrct)
      p <- rnode.proposeBlock
    } yield(d,p)
    println(s"computed deploy = ${computed}")
    computed.isLeft === false
  }
}
**/
