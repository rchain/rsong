package coop.rchain.rsong.proxy

import cats.effect._
import cats.implicits._
import org.http4s.server.blaze.BlazeBuilder
import api._
import coop.rchain.rsong.core.domain._
import coop.rchain.rsong.core.repo._
import repo._
import kamon.Kamon
import utils.Globals._
import scala.concurrent.duration.Duration
import kamon.prometheus.PrometheusReporter
import coop.rchain.rsong.core.utils.Globals._

object Bootstrap extends IOApp {

  def run(args: List[String]) =
    ServerStream.stream[IO].compile.drain.as(ExitCode.Success)

}
object ServerStream {
  import coop.rchain.rsong.proxy.api.middleware.MiddleWear._

  val proxy: RNodeProxy = RNodeProxy()
  val rnodeServer= Server(rnodeHost, rnodePort)
  val grpc = GRPC(rnodeServer)
  val redisServer: Server = Server(redisHost, redisPort)
  val repo = Repo(grpc, proxy)
  val cachedSongRepo: AssetCache = AssetCache(redisServer, repo)
  val cachedUserRepo: UserCache = UserCache(redisServer, UserRepo(grpc,proxy))

  def statusApi[F[_]: Effect] = new Status[F].routes
  def userApi[F[_]: Effect] = new UserApi[F](cachedUserRepo).routes
  def songApi[F[_]: Effect] =
    new SongApi[F](cachedSongRepo, cachedUserRepo).routes

  Kamon.addReporter(new PrometheusReporter())

  def stream[F[_]: ConcurrentEffect] =
    BlazeBuilder[F]
      .withIdleTimeout(Duration.Inf)
      .bindHttp(appCfg.getInt("api.http.port"), "0.0.0.0")
      .mountService(corsHeader(statusApi), s"/public")
      .mountService(corsHeader(statusApi), s"/")
      .mountService(corsHeader(userApi), s"/${apiVersion}/user")
      .mountService(corsHeader(songApi), s"/${apiVersion}")
      .serve
}
