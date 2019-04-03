package coop.rchain.rsong.proxy.api

import cats.effect._
import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.proxy.repo._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import io.circe.parser._

class IngestApi[F[_]: Sync](songRepo: AssetCache, userRepo: UserCache)
    extends Http4sDsl[F] {

  object perPage extends OptionalQueryParamDecoderMatcher[Int]("per_page")

  object page extends OptionalQueryParamDecoderMatcher[Int]("page")

  object userId extends QueryParamDecoderMatcher[String]("userId")

  val log = Logger[IngestApi[F]]

  val routes: HttpRoutes[F] =
    HttpRoutes.of[F] {
      case req @ POST -> Root / "ingest"  =>
        req.decode[String] { data => parse(data) match {
          case Right(j) => Ok(j)
          case Left(e) => BadRequest(e.message)
        }
        }
    }
}
