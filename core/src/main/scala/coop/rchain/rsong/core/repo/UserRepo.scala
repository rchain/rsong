package coop.rchain.rsong.core.repo

import com.typesafe.scalalogging.Logger
import coop.rchain.rsong.core.domain._

import scala.util._

trait UserRepo {
  def newUser(user: String): Either[Err, DeployAndProposeResponse]

  def decPlayCount(songId: String, userId: String): Either[Err, String]

  def fetchPlayCount(userId: String): Either[Err, PlayCount]

  def putPlayCountAtName(
      userId: String,
      playCountName: String): Either[Err, DeployAndProposeResponse]
}

object UserRepo {


  val COUNT_OUT = "COUNT-OUT"
  val log = Logger[UserRepo.type]

  def newUserRhoTerm(name: String): String =
    s"""@["Immersion", "newUserId"]!("${name}")"""

  def asInt(s: String): Either[Err, Int] = {
    Try(s.toInt) match {
      case Success(i) => Right(i)
      case Failure(e) =>
        Left(Err(OpCode.playCountConversion, e.getMessage))
    }
  }

  def apply(proxy: RNodeProxy) = new UserRepo {

    def newUser(user: String): Either[Err, DeployAndProposeResponse] ={
      val term = newUserRhoTerm(user)
      for{
      d <- proxy.deploy(term)
      p <- proxy.proposeBlock
      }  yield DeployAndProposeResponse(d,p)
}
    def putPlayCountAtName(
        userId: String,
        playCountOut: String): Either[Err, DeployAndProposeResponse] =
      for {
        rhoName <- proxy.dataAtName(s"""" $userId"""")
        playCountArgs = s"""("$rhoName".hexToBytes(), "$playCountOut")"""
        term = s"""@["Immersion", "playCount"]!${playCountArgs}"""
        d <- proxy.deploy(term)
        p <- proxy.deploy(term)
      } yield DeployAndProposeResponse(d,p)

    def fetchPlayCount(userId: String): Either[Err, PlayCount] = {
      val playCountOut = s"$userId-${COUNT_OUT}-${System.currentTimeMillis()}"
      val pc = for {
        _ <- putPlayCountAtName(userId, playCountOut)
        count <- proxy.dataAtName(s""""$playCountOut"""")
        countAsInt <- asInt(count)
      } yield PlayCount(countAsInt)
      log.info(s"userid: $userId has ${pc}")
      pc
    }

    def decPlayCount(songId: String, userId: String) = {
      val permittedOut =
        s"${userId}-${songId}-permittedToPlay-${System.currentTimeMillis()}"
      val pOut = for {
        sid <- proxy.dataAtName(s""""${songId}_Stereo.izr""""")
        uid <- proxy.dataAtName(s""""$userId"""")
        parameters = s"""("$sid".hexToBytes(), "$uid".hexToBytes(), "$permittedOut")"""
        term = s"""@["Immersion", "play"]!${parameters}"""
        _ <- proxy.deploy(term)
        _ <- proxy.proposeBlock
        p <- proxy.dataAtName(""""$permittedOut"""")
      } yield p
      log.info(s"user: $userId with song: $songId has permitedOut: $pOut")
      pOut
    }
  }
}
