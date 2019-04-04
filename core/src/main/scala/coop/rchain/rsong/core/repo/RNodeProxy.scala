package coop.rchain.rsong.core.repo

import coop.rchain.casper.protocol._
import coop.rchain.models.{Par, Expr}
import com.google.protobuf.empty._
import coop.rchain.rsong.core.domain._
import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.core.protocol.ParUtil._
import coop.rchain.models.either.EitherHelper._
import coop.rchain.rholang.interpreter.PrettyPrinter
import coop.rchain.rsong.core.domain.OpCode.OpCode
import coop.rchain.rsong.core.repo.GRPC.GRPC

import scala.util._


trait RNodeProxy {
  def deploy(contract: String): Either[Err, String]
  def proposeBlock: Either[Err, String]
  def dataAtName(name: String): Either[Err, String]
  def dataAtName(name: String, depth: Int): Either[Err, String]
}

object RNodeProxy {
  lazy val log = Logger[RNodeProxy.type]
  val MAXGRPCSIZE = 1024 * 1024 * 1024 * 5 // 10G for a song+metadat

  implicit class EitherOps(val resp: coop.rchain.either.Either) {
    def asEither: OpCode ⇒ Either[Err, String] =
      opcode ⇒ 
        resp match {
          case coop.rchain.either.Either(content) if content.isError ⇒ 
            Left(
              Err(opcode,
                  content.error
                    .map(x ⇒ x.messages.toString)
                    .getOrElse("No error message given!")))
          case coop.rchain.either.Either(content) if content.isEmpty ⇒
            Left(Err(opcode, "No value was returned!"))
          case coop.rchain.either.Either(content) if content.isSuccess ⇒
            Right(content.success.head.getResponse.value.toStringUtf8)
        }
  }

  def apply(grpc: GRPC): RNodeProxy = new RNodeProxy {

    def deploy(contract: String): Either[Err, String] = { 
      log.info(s"deploying contract: ${contract}")
      grpc
        .doDeploy(
          DeployData()
            .withTerm(contract)
            .withTimestamp(System.currentTimeMillis())
            .withPhloLimit(Long.MaxValue)
            .withPhloPrice(1L)
            .withValidAfterBlockNumber(-1)
        )
        .asEither(OpCode.grpcDeploy)
    }

    def proposeBlock: Either[Err, String] = 
      grpc.createBlock(Empty()).asEither(OpCode.grpcDeploy)

    def dataAtName(name: String): Either[Err, String] = dataAtName(name, Int.MaxValue)

    def dataAtName(name: String, depth: Int): Either[Err, String] = {
      name.asPar.flatMap(p ⇒ {
          val g = grpc.listenForDataAtName(DataAtNameQuery(depth, Some(p)))
          val _z = toEither[ListeningNameDataResponse](g)
                           val pars: Either[Seq[String], Seq[Par]] =
                             _z.map( x ⇒ x.blockResults.flatMap(y ⇒ y.postBlockData))
          val es: Either[Seq[String], Seq[Expr]] = pars.map(p ⇒ p.flatMap(_p ⇒ _p.exprs))
          val _es = es.map( x ⇒ x map(PrettyPrinter().buildString ) )
          _es match {
            case Left(z) ⇒ 
              Left(Err(OpCode.listenAtName, z.toString))
            case Right(r) if r.headOption.isDefined ⇒
              Right(r.head.trim)
            case Right(r) if r.headOption.isEmpty ⇒
              Left(Err(OpCode.nameNotFound, s"Empty String was returned! depth=$depth"))
          }
          })
    }
  }
}
