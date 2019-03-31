package coop.rchain.rsong.proxy.repo

import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.core.domain.{Err, Server}
import coop.rchain.rsong.proxy.domain.Domain.{CachedAsset, NameNotFoundEx}
import scalacache._
import scalacache.memoization._
import scalacache.modes.try_._
import scalacache.redis._
import scalacache.serialization.binary._
import coop.rchain.rsong.proxy.utils.ErrImplicits._

import scala.util._

trait AssetCache {
  val getMemoized: String => Either[Err, CachedAsset]
}

object AssetCache {
  val log = Logger[AssetCache.type]

  def apply(redisServer: Server, repo: Repo ) =
    new AssetCache {
      val binaryAsset: String => Either[Err, Array[Byte]] = name =>
        repo.queryBinaryAsset(name)

      implicit val __cache: Cache[CachedAsset] =
        RedisCache(redisServer.hostName, redisServer.port)

      __cache.config
      val getMemoized: String => Either[Err, CachedAsset] =
        name => {
          def __getMemoized(name: String): Try[CachedAsset] =
            memoize[Try, CachedAsset](None) {
              repo.queryBinaryAsset(name).map(CachedAsset(name, _)) match {
                case Right(s) =>
                  log.debug(s"Found asset. ${s}")
                  s
                case Left(e) =>
                  log.error(s"Exception in RSongCache layer. ${e}")
                  throw NameNotFoundEx(e.code.toString)
              }
            }

          log.info(s"in memoized, attempting to fetch $name")
          __getMemoized(name).asErr
        }
    }
}
