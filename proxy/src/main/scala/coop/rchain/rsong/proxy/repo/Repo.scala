package coop.rchain.rsong.proxy.repo

import coop.rchain.rsong.core.domain.{Err}
import coop.rchain.rsong.core.repo.{RNodeProxy}
import coop.rchain.rsong.core.utils.{Base16 => B16}

trait Repo {
  def queryAsset(name: String): Either[Err, String] 
  def queryBinaryAsset(name: String): Either[Err, Array[Byte]] =
    queryAsset(name) flatMap B16.decode
}

object Repo {
  def apply(proxy: RNodeProxy): Repo = new Repo {
    def queryAsset(nameIn: String): Either[Err, String] =
        proxy.dataAtName (s""""${nameIn}.out"""", Int.MaxValue)
 }
}
