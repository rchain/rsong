package coop.rchain.rsong.core.protocol

import com.typesafe.scalalogging.Logger
import coop.rchain.models.{Expr, GPrivate, Par}
import coop.rchain.rsong.core.domain.{Err, OpCode}
import monix.eval.Coeval

object ParUtil {

  val log = Logger("DeParConverter")

  import coop.rchain.rholang.interpreter.ParBuilder
  import ParBuilder._

  implicit class parOps(rTerm: String) {
    def asPar: Either[Err, Par] = {
      ParBuilder[Coeval].buildNormalizedTerm(rTerm).runAttempt match {
        case Left(e) =>
          println(e)
          log.error(s"String2Par failed with Exception: ${e.getMessage}")
          Left(Err(OpCode.nameToPar, e.getMessage))
        case Right(r) =>
          log.info(s"rTerm: ${rTerm} Par: ${r}")
          Right(r)
      }
    }
  }

}
