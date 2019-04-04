package coop.rchain.rsong.proxy.repo

import coop.rchain.rsong.core.repo._
import coop.rchain.rsong.core.domain._
import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.proxy.moc.MocSongMetadata._
import org.specs2._

class RepoSpec extends  Specification  { def is = s2"""
  proxy repo specsH
    proxy must store asset rholang name  $e1
    proxy must pre-fetch the asset rholang name  $e2
    proxy must fetch the asset rholang name  $e3
"""
  val log = Logger[RepoSpec]
  val assetRepo = AssetRepo(RNodeProxy(GRPC(Server("localhost", 40401))))
  val repo = Repo(assetRepo)

  def e1 = {
    val assetName = "Broke.jpg"
    val c = for {
      n ← assetRepo.dataAtName(s""""$assetName"""", Int.MaxValue)
      i ← assetRepo.dataAtName(s""""${assetName}.out"""", Int.MaxValue)
    } yield (i)

    log.info(s"computed data-size= ${c} ")
    c must beRight
  }


  def e2 = {
    val assetName = "Broke.jpg"
    val c = for {
      n ← assetRepo.dataAtName(s""""$assetName"""", Int.MaxValue)
      i ← assetRepo.dataAtName(s""""${assetName}.out"""", Int.MaxValue)
    } yield (i)

    log.info(s"computed data-size= ${c} ")
    c must beRight
    }

  def e3 = {
    val assetName = "Broke.jpg"
    val c = for {
      n ← assetRepo.dataAtName(s""""$assetName"""", Int.MaxValue)
      i ← assetRepo.dataAtName(s""""${assetName}.out"""", Int.MaxValue)
    } yield (i)

    log.info(s"computed data-size= ${c} ")
    c must beRight
  }

}
