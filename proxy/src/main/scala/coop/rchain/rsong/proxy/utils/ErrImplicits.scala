package coop.rchain.rsong.proxy.utils

import coop.rchain.rsong.core.domain.{Err, OpCode}
import coop.rchain.rsong.proxy.domain.Domain.CachingEx

import scala.util.{Either, Failure, Left, Right, Success, Try}

object ErrImplicits {
  implicit class _Either_[T](t: Try[T]) {
    def asErr: Either[Err, T] = {
      t match {
        case Success(s) => Right(s)
        case Failure(e)  if e.getMessage == OpCode.nameNotFound.toString =>
          Left(Err(OpCode.nameNotFound, "Name not found"))
        case Failure(e)  if e.getMessage == OpCode.unregisteredUser.toString =>
          Left(Err(OpCode.unregisteredUser, "unregistered user"))
        case Failure(e) =>
          Left(Err(OpCode.cacheLayer, e.getMessage))
      }
    }
  }

  implicit class _Try_[E, T](e: Either[E, T]) {
    def asTry: Try[T] = {
      e match {
        case Right(s) => Success(s)
        case Left(f) =>
          util.Failure(CachingEx(f.toString))
      }
    }
  }
}
