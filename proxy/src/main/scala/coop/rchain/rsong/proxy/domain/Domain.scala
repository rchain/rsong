package coop.rchain.rsong.proxy.domain
import coop.rchain.rsong.core.domain._

object Domain {

  case class CachingEx(message: String) extends Exception(message)
  case class NameNotFoundEx(message: String) extends Exception(message)


  case class RSong(
      id: String,
      isrc: String,
      iswc: String,
      cwr: String,
      upc: String,
      title: String,
      name: String,
      labelId: String,
      serviceId: String,
      featuredArtists: List[Artist],
      musician: List[String],
      language: String
  )

  case class Interval[T](from: T, to: Option[T])

  case class TemporalInterval(
    inMillis: Interval[Long],
    inUtc: Interval[String]
  )

  case class AuthorizedTerritory(
    territory: List[String],
    temporalInterval: TemporalInterval
  )

  case class Label(
    id: String,
    name: String,
    distributorName: String,
    authorizedTerritory: AuthorizedTerritory,
    distributorId: String,
    masterRecordingCollective: Boolean
  )

  case class ConsumptionModel(
    streaming: Boolean,
    downloadable: Boolean,
    conditionalDownload: Boolean,
    rentToOwn: Boolean,
    synchronizedWithPicture: Boolean
  )

  case class RSongMetadata(
    consumptionModel: ConsumptionModel,
    label: Label,
    song: RSong,
    artWorkId: String,
    album: Album
  )

  case class RSongAsset(
    rsong: RSong,
    typeOfAsset: String, //JPG, Stereo, 3D
    assetData: String,
    metadata: RSongMetadata,
    uri: String
  )

  object NameKey extends Enumeration {
    type NameKey = Value
    val newUserId, store, playCount, retrieveSong, retrieveMetadata, remunerate,
    play = Value
  }

  case class CachedUser(
    rsongUserId: String,
    playCount: PlayCount,
    isSyncWithRChain: Boolean = false,
    rChainName: Option[String] = None
  )

  case class CachedAsset(
    name: String,
    data: Array[Byte],
    isSyncWithRChain: Boolean = false,
    rChainName: Option[String] = None
  )

  case class User(id: String,
    name: Option[String],
    active: Boolean,
    lastLogin: Long,
    playCount: Int = 100,
    metadata: Map[String, String])

}
