package coop.rchain.rsong.core.domain

sealed trait Domain

case class RSongJsonAsset(
  id: String,
  assetData: String,
  jsonData: String
) extends Domain


trait Query {
  val nameIn: String
  val songId: String
  def nameOut: String
  def contract: String
}

case class SongQuery(
  nameIn: String,
  songId: String ) extends Query {
  def nameOut = s"${nameIn}.out"
  def contract: String =
    s"""
       |@["Immersion", "retrieveSong"]!("$songId".hexToBytes(), "$nameOut")
       |""".stripMargin.trim
}

object OpCode extends Enumeration {
  type OpCode = Value
  val grpcEval, grpcDeploy, grpcPropose, grpcShow, rholang, nameToPar,
  rsongHexConversion, rsongRetrival, rsongRevnetwork, contractFile,
  playCountConversion, nameNotFound, listenAtName, findName, cacheLayer, unregisteredUser,
  cachingSong, fileIO, unknown = Value
}

import OpCode._
import com.google.protobuf.ByteString


object TypeOfAsset {
  val t: Map[String, String] =
    Map("Stereo" -> "Stereo", "3D" -> "3D", "jpg" -> "jpg")
}
case class Err(code: OpCode, msg: String)

case class PlayCount(
  current: Int // init to 100
) extends Domain

case class DeployAndProposeResponse(fromDeploy: String, fromPropose: String)

case class Server(hostName: String, port: Int)

case class Artwork(id: String, uri: String) extends Domain

case class Artist(
  id: String,
  title: String,
  name: String
) extends Domain

case class Audio(
  effect: String,
  uri: String,
  duration_ms: Long
) extends Domain


case class Song(
  id: String,
  title: String,
  name: String,
  audio: List[Audio],
  language: String
) extends Domain

case class Album(
  id: String,
  title: String,
  name: String,
  artworks: List[Artwork],
  duration_ms: Long,
  artists: List[Artist],
  uri: String
) extends Domain

case class SongMetadata(
  song: Song,
  artists: List[Artist],
  artwork: List[Artwork],
  album: Option[Album] = None
) extends Domain

case class SongRequest(
  songId: String,
  userId: String
)

case class SongResponse(
  songMetadata: SongMetadata,
  playCount: PlayCount
)

case class DeParConverter(asInt: List[Int] = List(),
  asString: List[String] = List(),
  asUri: List[String] = List(),
  asByteArray: List[ByteString] = List())

