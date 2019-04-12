package coop.rchain.rsong.acq.utils

import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.core.utils.{Globals => G}

object Globals {
    private val log = Logger[Globals.type]
    val apiVersion = G.appCfg.getString("api.version")
    val artpath = s"$apiVersion/art"
    val songpath = s"$apiVersion/song/music"
    val (rnodeHost, rnodePort) = (G.appCfg.getString("grpc.host"),
      G.appCfg.getInt("grpc.ports.external"))
    val contractPath = G.appCfg.getString("contract.file.name")
    val rsongPath = G.appCfg.getString("assets.path")


    /**
    val rsongHostUrl: String = appCfg.getString("my.host.url")
    val (redisHost, redisPort) =
      (appCfg.getString("redis.host"), appCfg.getInt("redis.port"))
    **/

    log.info(s""""
              ----------------------------------------------------
             rnode server:   $rnodeHost:$rnodePort
              ----------------------------------------------------
  """)

}
