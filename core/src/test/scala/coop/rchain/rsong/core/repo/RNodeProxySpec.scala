package coop.rchain.rsong.core.repo

import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.core.domain.{Err, Server}
import coop.rchain.rsong.core.utils.{Base16 => B16}
import org.specs2.Specification
import coop.rchain.rsong.core.utils.{Globals ⇒ G }

class RNodeProxySpec extends  Specification {
  val log = Logger[RNodeProxySpec]
//  listen-for-name $e1
  // fetch by asset by name $e2
  // fetch previous run assets $e3
  
  def is = s2"""
      repo specs
          fetch  assets $e2
    """

  val grpc = GRPC(Server("localhost", 40401))
  val proxy: RNodeProxy =RNodeProxy()

  def e1 = {
    val computed =
      proxy.dataAtName(""""3a220a202cb8059faf2360a4f12e0cf19fca4768f44307dd4e2c9e0a7607201a63edf615-OUT"""")(grpc)
    log.info(s"dataAtName(broke.jpg)= ${computed}")
    println(s"---- dataAtName(broke.jpg)= ${computed}")
    (computed.isRight == true &&
      ! computed.right.get.isEmpty ) === true
  }

  def e2 = {
    val repo = AssetRepo(grpc, proxy)
    val computed: Either[Err, Array[Byte]] =
      repo.getAsset("Broke.jpg")
    log.info(s"getAsset(Broke.jpg) = ${computed}")
    computed must beRight
  }

  def e3  = {
    val assetName = "Broke.jpg"
    val computed = for {
      d ← proxy.dataAtName(s""""${assetName}-OUT"""" )(grpc)
       _ = log.info(s"postedBLockData for $assetName= = ${d}")
      b <- B16.decode(d)
    } yield (d)
    log.info(s"from prev. run: dataAtName(Broke.jpg) = ${computed}")
   computed must beRight
  }
}
