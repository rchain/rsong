package coop.rchain.rsong.core.utils

import org.specs2._
import coop.rchain.rsong.core.utils.{Base16 => B16}

class Base16Spec extends Specification { def is = s2"""
   Binary to String transformation specs
     to binary to/from String encoding/decoding $e1
   """

  def e1 = {
    val data = "hello world!".getBytes()

    val dataRoundTrip = for {
      h <- B16.encode(data)
      s <-  B16.decode(h)
    } yield (s)
    dataRoundTrip.isRight === true and dataRoundTrip.right.get === data
  }

}
