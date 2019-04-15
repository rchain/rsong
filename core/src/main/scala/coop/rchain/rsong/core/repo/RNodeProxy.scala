package coop.rchain.rsong.core.repo

import com.google.protobuf.empty._
import com.google.protobuf.ByteString
import coop.rchain.rsong.core.domain._
import com.typesafe.scalalogging.Logger
import coop.rchain.models.either.EitherHelper._
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
    val publicKey  = PublicKey(
      Base16.unsafeDecode(
        Globals.appCfg.getString("bond.key.public")))
    val privateKey = PrivateKey(
      Base16.unsafeDecode(
        Globals.appCfg.getString("bond.key.private")))
    for {
      c ← FileUtil.fileFromClasspath(filePath)
      d ← deploy(RholangContract(
                   code = c,
                   publicKey = publicKey,
                   privateKey = privateKey))(grpc)
    } yield (d)
  }

  def proposeBlock: GRPC ⇒ Either[Err, String]

  def dataAtName(name: String): GRPC ⇒ Either[Err, String] =
    grpc ⇒ dataAtName(name, Int.MaxValue)(grpc)

  def dataAtName(name: String, depth: Int): GRPC ⇒ Either[Err, String]

  def doDataAtName(name: String, depth: Int): ConfigReader[EE] =
    Reader(cfg ⇒ dataAtName(name, depth)(cfg))

  def doDataAtName(name: String): ConfigReader[EE] =
    doDataAtName(name, Int.MaxValue)

  def doDeploy(contract: RholangContract): ConfigReader[EE] =
    Reader(cfg ⇒ deploy(contract)(cfg))

  def doDeploys(contracts: List[RholangContract]): ConfigReader[List[EE]] =
    contracts.traverse(doDeploy)

  def doDeployFile(filePath: String): ConfigReader[EE] =
    Reader(cfg ⇒ deployFile(filePath)(cfg))

  def doProposeBlock: ConfigReader[EE] = Reader(cfg ⇒ proposeBlock(cfg))

  def runWork(work: ConfigReader[EE]): GRPC ⇒ EE = grpc ⇒ work.run(grpc)

  def lift(ee: EE): ConfigReader[EE] = Reader(cfg => ee)
}

object RNodeProxy {

  import coop.rchain.rsong.core.repo.RChainToRsongHelper._

  lazy val log = Logger[RNodeProxy.type]

  def sign(deploy: DeployData, sec: PrivateKey): DeployData =
    SignDeployment.sign(sec, deploy, Ed25519)

  def apply(): RNodeProxy = new RNodeProxy {

    def deploy(contract: RholangContract): GRPC ⇒ Either[Err, String] = grpc ⇒ {
      log.info(s"deploying contract: ${contract}")
      val data =
        DeployData()
          .withTerm(contract.code)
          .withTimestamp(System.currentTimeMillis())
          .withPhloPrice(0L)
          .withPhloLimit(Long.MaxValue)
          .withDeployer(ByteString.copyFrom(
            Ed25519.toPublic(contract.privateKey).bytes))
      val s = sign(data, contract.privateKey)
      grpc
        .doDeploy(s)
        .asEither(OpCode.grpcDeploy)
    }

    def proposeBlock: GRPC ⇒ Either[Err, String] = grpc ⇒ grpc.createBlock(Empty()).asEither(OpCode.grpcDeploy)

    def dataAtName(name: String, depth: Int): GRPC ⇒ Either[Err, String] = grpc ⇒ {
      val par = name.asPar
      val dataAtNameQuery = DataAtNameQuery(depth, Some(par))
      log.info(s"name =$name   par=${par}  dataAtNameQuery = ${dataAtNameQuery}")
      val g = grpc.listenForDataAtName(dataAtNameQuery)
      val _g: Either[Seq[String], ListeningNameDataResponse] =
        toEither[ListeningNameDataResponse](g)
      _g.asEitherString
    }
  }
}
