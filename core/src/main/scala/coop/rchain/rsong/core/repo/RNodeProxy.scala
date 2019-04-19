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
import coop.rchain.crypto.{PrivateKey, PublicKey}
import RNodeProxyTypeAlias._
import cats.data._
import cats.implicits._
import coop.rchain.crypto.codec.Base16
import coop.rchain.rsong.core.utils.{Base16 ⇒ B16}
import coop.rchain.rsong.core.utils.Globals
import coop.rchain.rsong.core.utils.FileUtil
import coop.rchain.either.{Either => RCEither}

object RNodeProxyTypeAlias {
  type BinData = Array[Byte]
  type EEBin = Either[Err, BinData]
  type EEString = Either[Err, String]
  type ConfigReader[A] = Reader[GRPC, A]
}

trait RNodeProxy {

  def deploy(contract: RholangContract): GRPC ⇒ Either[Err, String]

  def deployFile(filePath: String): GRPC ⇒ Either[Err, String] = grpc => {
    val publicKey =
      PublicKey(
        Base16.unsafeDecode(Globals.appCfg.getString("bond.key.public"))
      )
    val privateKey =
      PrivateKey(
        Base16.unsafeDecode(Globals.appCfg.getString("bond.key.private"))
      )
    for {
      c ← FileUtil.fileFromClasspath(filePath)
      d ← deploy(
        RholangContract(
          code = c,
          publicKey = publicKey,
          privateKey = privateKey
        )
      )(grpc)
    } yield (d)
  }

  def proposeBlock: GRPC ⇒ Either[Err, String]

  def dataAtName(name: String): GRPC ⇒ Either[Err, String] =
    grpc ⇒ dataAtName(name, Int.MaxValue)(grpc)

  def dataAtName(name: String, depth: Int): GRPC ⇒ Either[Err, String]

  def binDataAtName(name: String, depth: Int): GRPC ⇒ Either[Err, Array[Byte]] =
    grpc ⇒ dataAtName(name, depth)(grpc).flatMap(B16.decode)

  def binDataAtName(name: String): GRPC ⇒ Either[Err, Array[Byte]] =
    grpc ⇒ dataAtName(name)(grpc).flatMap(B16.decode)

  def doBinDataAtName(name: String, depth: Int): ConfigReader[EEBin] =
    Reader(cfg ⇒ binDataAtName(name, depth)(cfg))
  def doBinDataAtName(name: String): ConfigReader[EEBin] =
    doBinDataAtName(name, Int.MaxValue)

  def doDataAtName(name: String, depth: Int): ConfigReader[EEString] =
    Reader(cfg ⇒ dataAtName(name, depth)(cfg))

  def doDataAtName(name: String): ConfigReader[EEString] =
    doDataAtName(name, Int.MaxValue)

  def doDeploy(contract: RholangContract): ConfigReader[EEString] =
    Reader(cfg ⇒ deploy(contract)(cfg))

  def doDeploys(
      contracts: List[RholangContract]
  ): ConfigReader[List[EEString]] =
    contracts.traverse(doDeploy)

  def doDeployFile(filePath: String): ConfigReader[EEString] =
    Reader(cfg ⇒ deployFile(filePath)(cfg))

  def doProposeBlock: ConfigReader[EEString] = Reader(cfg ⇒ proposeBlock(cfg))

  def runWork(work: ConfigReader[EEString]): GRPC ⇒ EEString =
    grpc ⇒ work.run(grpc)

  def lift(ee: EEString): ConfigReader[EEString] = Reader(cfg => ee)
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
          .withDeployer(
            ByteString.copyFrom(Ed25519.toPublic(contract.privateKey).bytes)
          )
      val s = sign(data, contract.privateKey)
      grpc
        .doDeploy(s)
        .asEither(OpCode.grpcDeploy)
    }

    def proposeBlock: GRPC ⇒ Either[Err, String] =
      grpc ⇒ grpc.createBlock(Empty()).asEither(OpCode.grpcDeploy)

    def dataAtName(name: String, depth: Int): GRPC ⇒ Either[Err, String] =
      grpc ⇒ {
        val par = name.asPar
        val dataAtNameQuery = DataAtNameQuery(depth, Some(par))

        def helper(cnt: Int):Either[Err, String]  = {
          val g: RCEither = grpc.listenForDataAtName(dataAtNameQuery)
          val _g: Either[Seq[String], ListeningNameDataResponse] =
            toEither[ListeningNameDataResponse](g)
          _g.asEitherString  match {
            case Left(e) if cnt <20 ⇒
              log.warn(s"dataAtName try: $cnt returned ${e}. attempting again")
              helper(cnt+1)
            case Left(e)  ⇒ 
              log.warn(s"dataAtName try: $cnt returned ${e}. Exhosted all attempts. giving up!")
              Left(e)
            case Right(r) ⇒ Right(r) 
          }
        }
        helper(0)
      }
  }
}
