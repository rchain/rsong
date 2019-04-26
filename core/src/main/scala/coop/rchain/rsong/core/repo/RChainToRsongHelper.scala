package coop.rchain.rsong.core.repo

import coop.rchain.casper.protocol.ListeningNameDataResponse
import coop.rchain.models.Expr.ExprInstance.GString
import coop.rchain.models.{Expr, Par}
import coop.rchain.rholang.interpreter.PrettyPrinter
import coop.rchain.rsong.core.domain.OpCode.OpCode
import coop.rchain.rsong.core.domain.{Err, OpCode}

object RChainToRsongHelper {

  implicit class EitherOps(
      val resp: coop.rchain.either.Either
  ) {
    def asEither: OpCode ⇒ Either[Err, String] =
      opcode ⇒
        resp match {
          case coop.rchain.either.Either(content) if content.isError ⇒
            Left(
              Err(
                opcode,
                content.error
                  .map(x ⇒ x.messages.toString)
                  .getOrElse("No error message given!")
              )
            )
          case coop.rchain.either.Either(content) if content.isEmpty ⇒
            Left(Err(opcode, "No value was returned!"))
          case coop.rchain.either.Either(content) if content.isSuccess ⇒
            val res: String = content.success.headOption.headOption
              .map(x => x.getResponse.value.toStringUtf8)
              .getOrElse("")
            if (res.isEmpty)
              Left(
                Err(opcode, "empty string was returned!")
              )
            else Right(res)
        }
  }

  implicit class ListeningNameDataResponseOps(
      response: Either[Seq[String], ListeningNameDataResponse]
  ) {
    def asEitherString =
      response match {
        case Left(strs) ⇒
          Left(Err(OpCode.listenAtName, strs.toString))
        case Right(ListeningNameDataResponse(_, length)) if length < 1 ⇒
          Left(
            Err(
              OpCode.listenAtName,
              s"dataAtName length <1.  length = $length"
            )
          )
        case Right(
            ListeningNameDataResponse(blockResults, _)
            ) if blockResults.isEmpty ⇒
          Left(
            Err(
              OpCode.listenAtName,
              s"dataAtName blockResults isEmpty.Returned val = ${response}"
            )
          )
        case Right(
            ListeningNameDataResponse(blockResults, _)
            ) ⇒
          blockResults
            .flatMap(x => x.postBlockData.par)
            .headOption match {
            case None ⇒
              Left(
                Err(
                  OpCode.listenAtName,
                  s"dataAtName blockResult.PostData.Par = empty-List.  response = ${response}"
                )
              )
            case Some(r) ⇒
              PrettyPrinter().buildString(r) match {
                case s: String if s.isEmpty =>
                  Left(
                    Err(
                      OpCode.listenAtName,
                      s"dataAtName prettyPrinter build empty string.response = ${response}"
                    )
                  )
                case s: String ⇒ Right(s)
              }
          }
      }
  }

  implicit class parOps(rTerm: String) {
    def asPar: Par =
      Par().copy(exprs = Seq(Expr(GString(rTerm))))
  }

}
