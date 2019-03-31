package coop.rchain.rsong.core.utils

import java.io.{BufferedInputStream, FileInputStream, InputStream}
import coop.rchain.rsong.core.utils.{Base16 ⇒ B16 }

import coop.rchain.rsong.core.domain.{Err, OpCode}

import scala.util._

object FileUtil {

  val fileFromClasspath: String => Either[Err, String] = fileName => {
    val stream = getClass.getResourceAsStream(fileName)
    Try(
      scala.io.Source.fromInputStream(stream).getLines.reduce(_ + _ + "\n")
    ) match {
      case Success(s) =>
        stream.close
        Right(s)
      case Failure(e) =>
        stream.close
        Left(Err(OpCode.contractFile, e.getMessage))
    }
  }
  def readStreamAsByteArray(stream: InputStream): Array[Byte] = {
    val bis = new BufferedInputStream(stream)
    Stream.continually(bis.read).takeWhile(-1 !=).map(_.toByte).toArray
  }
  def readFileAsByteArray(fileName: String): Either[Err, Array[Byte]] = {
    Try{
    val bis = new BufferedInputStream(new FileInputStream(fileName))
    Stream.continually(bis.read).takeWhile(-1 !=).map(_.toByte).toArray
    } match {
      case Success(s) => Right(s)
      case Failure(e) => Left(Err(OpCode.fileIO, e.getMessage))
    }
  }

  def logDepth(s: String): String = {
    val threshold = 1024
    if (s.length <= threshold)
      s""""$s""""
    else {
      val mid = s.length / 2
      val l = logDepth(s.substring(0, mid))
      val r = logDepth(s.substring(mid))
      s"""(\n$l\n++\n$r\n)"""
    }
  }

  def asHexConcatRsong(filePath: String): Either[Err, String] = {
    for{
    f <- readFileAsByteArray(filePath)
    e <- B16.encode(f)
    s = logDepth(e)
    } yield (s)
  }
}
