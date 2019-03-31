package coop.rchain.rsong.core.repo

import coop.rchain.casper.protocol._
import coop.rchain.models.{Par, Expr}
import com.google.protobuf.empty._
import io.grpc.{ManagedChannel, ManagedChannelBuilder}
import coop.rchain.rsong.core.domain._
import com.typesafe.scalalogging.Logger
import coop.rchain.either.{Either ⇒ CE}
import coop.rchain.rsong.core.protocol.ParUtil._
import coop.rchain.models.either.EitherHelper._
import coop.rchain.rholang.interpreter.PrettyPrinter
import coop.rchain.rsong.core.domain.OpCode.OpCode
import coop.rchain.rsong.core.repo.GRPC.GRPC

import scala.util._


trait RNodeProxy {
  def deploy(contract: String): GRPC ⇒ Either[Err, String]
  def proposeBlock: GRPC ⇒ Either[Err, String]
  def dataAtName(name: String): GRPC ⇒ Either[Err, String]
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

  def apply(): RNodeProxy = new RNodeProxy {

    def deploy(contract: String): GRPC ⇒ Either[Err, String] = { grpc ⇒
      log.debug(s"deploying contract: ${contract}")
      grpc
        .doDeploy(
          DeployData()
            .withTerm(contract)
            .withTimestamp(System.currentTimeMillis())
            .withPhloLimit(Long.MaxValue)
            .withPhloPrice(1L)
        )
        .asEither(OpCode.grpcDeploy)
    }

    def proposeBlock: GRPC ⇒ Either[Err, String] = grpc ⇒
      grpc.createBlock(Empty()).asEither(OpCode.grpcDeploy)

    def dataAtName(name: String): GRPC ⇒ Either[Err, String] =  grpc ⇒ {
      name.asPar.flatMap(p ⇒ {
          val g = grpc.listenForDataAtName(DataAtNameQuery(Int.MaxValue, Some(p)))
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
              Left(Err(OpCode.nameNotFound, "Empty String was returned!"))
          }
          })
    }
    def _dataAtName(name: String): GRPC => Either[Err, String] = { grpc ⇒
      name.asPar.flatMap(par ⇒ {
        val res: CE =
          grpc.listenForDataAtName(DataAtNameQuery(Int.MaxValue, Some(par)))
        toEither[ListeningNameDataResponse](res) match {
          case e if e.isRight ⇒
            val r = for {
              x ← e.right.get.blockResults
              y ← x.postBlockData
              z = PrettyPrinter().buildString(y)
            } yield (z)
            r.headOption match {
              case Some(s) ⇒ Right(s.trim)
              case None ⇒
                Left(Err(OpCode.nameNotFound, s"$name was not found"))
            } case e if e.isLeft ⇒
            Left(Err(OpCode.listenAtName, e.left.get.toString))
        }
      })
    }
  }
}
