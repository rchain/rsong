package coop.rchain.rsong.acq.domain

sealed trait Query {
  val nameIn: String
  val songId: String
  def nameOut: String
  def contract: String
}
case class SongQuery(nameIn: String, songId: String)
    extends Query {
  def nameOut = s"${nameIn}.out"
  def contract: String =
    s"""
       |@["Immersion", "retrieveSong"]!("$songId".hexToBytes(), "$nameOut")
       |""".stripMargin.trim
}
