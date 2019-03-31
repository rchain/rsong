package coop.rchain.rsong.core.utils

import com.typesafe.config.ConfigFactory

object Globals {
  lazy val cfg = ConfigFactory.load
  lazy val appCfg = cfg.getConfig("coop.rchain.rsong")
}
