package coop.rchain.rsong.core.repo

import coop.rchain.rsong.core.domain.{Err, OpCode, Query}
import com.typesafe.scalalogging.Logger

trait AssetRepo {
  def deploy(s: String): Either[Err, String]
  def proposeBlock: Either[Err, String]
  def setDataAtName(q: Query): Either[Err, Unit]
  def dataAtName(name: String, depth: Int) : Either[Err, String]
  def findDataAtName(name: String)(maxDepth: Int) : Either[Err, String] = {
    def helper(depth: Int) : Either[Err, String] = {
      println(s"___DEPTH = $depth.  name= $name ---")
      dataAtName(name, depth) match {
        case Left(Err(OpCode.nameNotFound, _)) if depth < maxDepth => helper(depth+1)
        case Right(r) => Right(r)
        case Left(e) => Left(e)
      }
      }
    helper( 0)
  }

  def getAsset(q: Query): Either[Err, String] =
    for {
      _ ← setDataAtName(q)
      asStr ← findDataAtName(s""""${q.nameOut}"""")(20)
    } yield (asStr)
  
}

object AssetRepo {
  lazy val log = Logger[AssetRepo.type]

  def apply(proxy: RNodeProxy): AssetRepo = new AssetRepo {
    def dataAtName(name: String, depth: Int): Either[Err, String] =
      proxy.dataAtName(name, depth)

    def setDataAtName(q: Query): Either[Err, Unit] =  
      for { 
      _ ← proxy.deploy(q.contract)
      _ ← proxy.proposeBlock
      _ = log.info(s"contract → ${q.contract} was deployed and proposed.")
      } yield()

    def deploy(s: String): Either[Err, String] = proxy.deploy(s)
    def proposeBlock: Either[Err, String] = proxy.proposeBlock
  }
}
