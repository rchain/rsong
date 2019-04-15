package coop.rchain.rsong.core.utils

import java.math.BigInteger
import java.security.MessageDigest

import coop.rchain.rsong.core.utils.FileUtil._
import org.specs2.Specification

class FileUtilSpec extends Specification {
  def is = s2"""
  fileutils specs
    read rsong contract $e1
    binary asset to transformation $e3
 """
  val md = MessageDigest.getInstance("MD5")

  def e1 = {

    val computed = fileFromClasspath("/rho/rsong.rho")
    computed.isRight === true
  }
  def e2 = {
    val binFile          = "data/Broke.jpg"
    val binAssetAsString = asHexConcatRsongFromFile(binFile)
    binAssetAsString must beRight
//    (binAssetAsString map Base16.unsafeDecode) must beRight
  }

  def e3 = {
    val binAsset: Array[Byte] = readStreamAsByteArray(getClass.getResourceAsStream("/data/Broke.jpg"))
    val binConverted = for {
      e <- Base16.encode(binAsset)
      s <- Base16.decode(e)
    } yield (s)

    val computed = binConverted map md5HashString
    computed must beRight
    computed.right.get === md5HashString(binAsset)
    binAsset.length !== 0
  }
  def md5HashString(s: Array[Byte]): BigInteger =
    new BigInteger(md.digest(s))
}
