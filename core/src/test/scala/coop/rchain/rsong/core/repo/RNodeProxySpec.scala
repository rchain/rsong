package coop.rchain.rsong.core.repo

import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.core.domain.{Err, Server}
import coop.rchain.rsong.core.utils.{Base16 => B16}
import org.specs2.Specification
import coop.rchain.rsong.core.utils.{Globals ⇒ G }
import coop.rchain.rsong.core.domain._

class RNodeProxySpec extends  Specification { def is = s2"""
      repo specs
          fetch  assets $e1
    """
//  listen-for-name $e1
  // fetch by asset by name $e2
  // fetch previous run assets $e3
  

  val log = Logger[RNodeProxySpec]
  val grpc = GRPC(Server("localhost", 40401))
  val proxy: RNodeProxy =RNodeProxy(grpc)

  def e1 = {
    val rhoNameIn= proxy.dataAtName(""""Broke.jpg"""")
    val rhoNameOut =  proxy.dataAtName(""""Broke.jpg.out"""")
    log.info(s"dataAtName(broke.jpg)= ${rhoNameIn}")
    println(s"---- dataAtName(broke.jpg.out)= ${rhoNameOut}")
    (rhoNameOut.isRight && rhoNameOut.right.get.isDefinedAt(0) ) ===  true
  }

  // def e2 = {
  //   val repo = AssetRepo(proxy)
  //   val rhoNameIn= proxy.dataAtName(""""Broke.jpg"""")
  //   val query: Query = SongQuery("Broke.jpg", rhoNameIn)
  //   log.info(s"rho contract to songQuery: ${query} = ${query.contract}")
  //   val computed: Either[Err, Array[String]] =
  //     repo.getAsset(query)
  //   log.info(s"getAsset(Broke.jpg) = ${computed}")
  //   computed must beRight
  // }

  def e3  = {
    val assetName = "Broke.jpg"
    val computed = for {
      d ← proxy.dataAtName(s""""${assetName}"""" )
       _ = log.info(s"postedBLockData for $assetName= = ${d}")
      b <- B16.decode(d)
    } yield (d)
    log.info(s"from prev. run: dataAtName(Broke.jpg) = ${computed}")
   computed must beRight
  }
}
