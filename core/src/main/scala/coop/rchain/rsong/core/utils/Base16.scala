package coop.rchain.rsong.core.utils

import coop.rchain.rsong.core.domain.{Err, OpCode}
import scala.util._

object Base16 {
  def encode(input: Array[Byte]): Either[Err, String] = bytes2hex(input, None) match {
    case (s) if  s.isEmpty =>
      Left(Err(OpCode.rsongHexConversion, s"bytes2hex returned empty-string!. Orig payload: ${input}"))
    case s => Right(s)
  }

  def decode(input: String): Either[Err, Array[Byte] ]= {
    val paddedInput =
      if (input.length % 2 == 0) input
      else "0" + input

    Try( hex2bytes(paddedInput) ) match {
      case Success(v) => Right(v)
      case Failure(e) => Left(Err(OpCode.rsongHexConversion, s"hex2bytes ${e.getMessage} -- ${input}"))
    }
  }

  private def bytes2hex(bytes: Array[Byte], sep: Option[String]): String =
    bytes.map("%02x".format(_)).mkString(sep.getOrElse(""))

  private def hex2bytes(hex: String): Array[Byte] =
    hex
      .replaceAll("[^0-9A-Fa-f]", "")
      .sliding(2, 2)
      .toArray
      .map(Integer.parseInt(_, 16).toByte)
}

