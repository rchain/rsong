package coop.rchain.rsong.acq.domain

import coop.rchain.rsong.core.domain.SongMetadata

object Domain {
  case class Asset(
    id: String,
    uri: String,
    metadata: SongMetadata
  )

  object TypeOfAsset {
    val t: Map[String, String] =
      Map("Stereo" -> "Stereo", "3D" -> "3D", "jpg" -> "jpg")
  }
}
