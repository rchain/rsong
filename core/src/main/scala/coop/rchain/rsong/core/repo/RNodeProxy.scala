package coop.rchain.rsong.core.repo

import coop.rchain.rsong.core.protocol.ParUtil._
import coop.rchain.models.{ Expr, Par }
import com.google.protobuf.empty._
import com.google.protobuf.ByteString
import coop.rchain.rsong.core.domain._
import com.typesafe.scalalogging.Logger
import coop.rchain.models.either.EitherHelper._
import coop.rchain.rholang.interpreter.PrettyPrinter
import coop.rchain.rsong.core.domain.OpCode.OpCode
import coop.rchain.rsong.core.repo.GRPC.GRPC
import coop.rchain.casper.protocol._
import coop.rchain.casper.SignDeployment
import coop.rchain.crypto.signatures.Ed25519
import coop.rchain.crypto.{ PrivateKey, PublicKey }
import RNodeProxyTypeAlias._
import cats.data._
import cats.implicits._
import coop.rchain.crypto.codec.Base16
import coop.rchain.rsong.core.utils.Globals
import coop.rchain.rsong.core.utils.FileUtil

object RNodeProxyTypeAlias {
  type EE              = Either[Err, String]
  type ConfigReader[A] = Reader[GRPC, A]
}

trait RNodeProxy {
  def deploy(contract: RholangContract): GRPC ⇒ Either[Err, String]
  def deployFile(filePath: String): GRPC ⇒ Either[Err, String] = grpc => {
    val publicKey  = PublicKey(Base16.unsafeDecode(Globals.appCfg.getString("bond.key.public")))
    val privateKey = PrivateKey(Base16.unsafeDecode(Globals.appCfg.getString("bond.key.private")))
    for {
      c ← FileUtil.fileFromClasspath(filePath)
      d ← deploy(RholangContract(code = c, publicKey = publicKey, privateKey = privateKey))(grpc)

    } yield (d)
  }
  def proposeBlock: GRPC ⇒ Either[Err, String]
  def dataAtName(name: String): GRPC ⇒ Either[Err, String] = grpc ⇒ dataAtName(name, Int.MaxValue)(grpc)
  def dataAtName(name: String, depth: Int): GRPC ⇒ Either[Err, String]
  def doDeploy(contract: RholangContract): ConfigReader[EE] =
    Reader(cfg ⇒ deploy(contract)(cfg))
  def doDeploys(contracts: List[RholangContract]): ConfigReader[List[EE]] =
    contracts.traverse(doDeploy)
  def doDeployFile(filePath: String): ConfigReader[EE] =
    Reader(cfg ⇒ deployFile(filePath)(cfg))

  def doProposeBlock: ConfigReader[EE] = Reader(cfg ⇒ proposeBlock(cfg))
}

object RNodeProxy {
  lazy val log    = Logger[RNodeProxy.type]
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
                    .getOrElse("No error message given!"))
            )
          case coop.rchain.either.Either(content) if content.isEmpty ⇒
            Left(Err(opcode, "No value was returned!"))
          case coop.rchain.either.Either(content) if content.isSuccess ⇒
            Right(content.success.head.getResponse.value.toStringUtf8)
      }
  }

  def sign(deploy: DeployData, sec: PrivateKey): DeployData =
    SignDeployment.sign(sec, deploy, Ed25519)

  def apply(): RNodeProxy = new RNodeProxy {

    def deploy(contract: RholangContract): GRPC ⇒ Either[Err, String] = grpc ⇒ {
      log.info(s"deploying contract: ${contract}")
      val data =
        DeployData()
          .withTerm(contract.code)
          .withPhloLimit(Long.MaxValue)
          .withTimestamp(System.currentTimeMillis())
          .withPhloPrice(1L)
          .withDeployer(ByteString.copyFrom(Ed25519.toPublic(contract.privateKey).bytes))
      val s = sign(data, contract.privateKey)
      grpc
        .doDeploy(s)
        .asEither(OpCode.grpcDeploy)
    }

    def proposeBlock: GRPC ⇒ Either[Err, String] = grpc ⇒ grpc.createBlock(Empty()).asEither(OpCode.grpcDeploy)

    def dataAtName(name: String, depth: Int): GRPC ⇒ Either[Err, String] = grpc ⇒ {
      name.asPar.flatMap(p ⇒ {
        val g  = grpc.listenForDataAtName(DataAtNameQuery(depth, Some(p)))
        val _z = toEither[ListeningNameDataResponse](g)
        val pars: Either[Seq[String], Seq[Par]] =
          _z.map(x ⇒ x.blockResults.flatMap(y ⇒ y.postBlockData))
        val es: Either[Seq[String], Seq[Expr]] = pars.map(p ⇒ p.flatMap(_p ⇒ _p.exprs))
        val _es                                = es.map(x ⇒ x map (PrettyPrinter().buildString))
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
