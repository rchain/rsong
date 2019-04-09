package coop.rchain.rsong.acq.domain
import coop.rchain.rsong.core.domain.SongMetadata

object Domain {
  case class RsongAsset(
    id: String,
    uri: String,
    metadata: SongMetadata
  )

}
