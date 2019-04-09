package coop.rchain.rsong.acq.moc

import coop.rchain.rsong.acq.domain.Domain.RsongAsset
import coop.rchain.rsong.core.domain._

object MocSongMetadata {

  val artpath  = "v1/art"
  val songpath = "v1/song/music"
  //  val rsongHostUrl: String = "http://dev-rchain.com"
  val rsongHostUrl: String = java.net.InetAddress.getLocalHost.getHostName

  val artworks = Map(
    "Broke"      -> Artwork(id = "Broke", uri = s"${rsongHostUrl}/${artpath}/Broke.jpg"),
    "Euphoria"   -> Artwork(id = "Euphoria", uri = s"${rsongHostUrl}/${artpath}/Euphoria.jpg"),
    "Tiny_Human" -> Artwork(id = "Tiny_Human", uri = s"${rsongHostUrl}/${artpath}/Tiny Human.jpg")
  )
  val artists = Map(
    "Broke" -> Artist(id = "Mycle-Wastman", name = "Mycle Wastman", title = "Mycle Wastman"),
    "Euphoria" -> Artist(id = "California-Guitar-Trio",
                         name = "California Guitar Trio",
                         title = "California Guitar Trio"),
    "Tiny_Human" -> Artist(id = "Imogen-Heap", name = "Imogen Heap", title = "Imogen Heap")
  )

  val albums = Map(
    "Broke" -> Album(
      id = "Broke",
      artworks = List(artworks("Broke")),
      name = "Broke",
      title = "Broke",
      duration_ms = 10000,
      artists = List(artists("Broke")),
      uri = s"${rsongHostUrl}/${artpath}/Broke.jpg"
    ),
    "Euphoria" -> Album(
      id = "Euphoria",
      artworks = List(artworks("Euphoria")),
      name = "Euphoria",
      title = "Euphoria",
      duration_ms = 10000,
      artists = List(artists("Euphoria")),
      uri = s"${rsongHostUrl}/${artpath}/Euphoria.jpg"
    ),
    "Tiny_Human" -> Album(
      id = "album-id-Tiny_Human",
      artworks = List(artworks("Tiny_Human")),
      name = "Tiny Human",
      title = "Tiny Human",
      duration_ms = 10000,
      artists = List(artists("Tiny_Human")),
      uri = s"${rsongHostUrl}/${artpath}/Tiny Human.jpg"
    )
  )

  val song = Map(
    "Broke" ->
      Song(
        id = "Broke",
        name = "Broke",
        title = "Broke",
        audio = List(
          Audio(effect = TypeOfAsset.t("3D"),
                uri = s"${rsongHostUrl}/${songpath}/Broke_Immersive.izr",
                duration_ms = 1000L),
          Audio(effect = TypeOfAsset.t("Stereo"),
                uri = s"${rsongHostUrl}/${songpath}/Broke_Stereo.izr",
                duration_ms = 1000L)
        ),
        language = "EN"
      ),
    "Tiny_Human" ->
      Song(
        id = "Tiny_Human",
        name = "Tiny Human",
        title = "Tiny Human",
        audio = List(
          Audio(effect = TypeOfAsset.t("3D"),
                uri = s"${rsongHostUrl}/${songpath}/Tiny_Human_Immersive.izr",
                duration_ms = 1000L),
          Audio(effect = TypeOfAsset.t("Stereo"),
                uri = s"${rsongHostUrl}/${songpath}/Tiny_Human_Stereo.izr",
                duration_ms = 1000L)
        ),
        language = "EN"
      ),
    "Euphoria" ->
      Song(
        id = "Euphoria",
        name = "Euphoria",
        title = "Euphoria",
        audio = List(
          Audio(effect = TypeOfAsset.t("3D"),
                uri = s"${rsongHostUrl}/${songpath}/Euphoria_Immersive.izr",
                duration_ms = 1000L),
          Audio(effect = TypeOfAsset.t("Stereo"),
                uri = s"${rsongHostUrl}/${songpath}/Euphoria_Stereo.izr",
                duration_ms = 1000L)
        ),
        language = "EN"
      )
  )

  val mocSongs = Map(
    ("Broke" ->
      SongMetadata(song("Broke"),
                   artists = List(artists("Broke")),
                   artwork = List(artworks("Broke")),
                   album = Some(albums("Broke")))),
    ("Euphoria" ->
      SongMetadata(song("Euphoria"),
                   artists = List(artists("Euphoria")),
                   artwork = List(artworks("Euphoria")),
                   album = Some(albums("Euphoria")))),
    ("Tiny_Human" ->
      SongMetadata(song("Tiny_Human"),
                   artists = List(artists("Tiny_Human")),
                   artwork = List(artworks("Tiny_Human")),
                   album = Some(albums("Tiny_Human"))))
  )

  def assets(path: String): Seq[RsongAsset] =
    Seq(
      RsongAsset("Broke.jpg", s"$path/Labels/Broke.jpg", mocSongs("Broke")),
      RsongAsset("Euphoria.jpg", s"$path/Labels/Euphoria.jpg", mocSongs("Euphoria")),
      RsongAsset("Tiny Human.jpg", s"$path/Labels/Tiny Human.jpg", mocSongs("Tiny_Human")),
      RsongAsset("Broke_Immersive.izr", s"$path/Songs/Broke_Immersive.izr", mocSongs("Broke")),
      RsongAsset("Broke_Stereo.izr", s"$path/Songs/Broke_Stereo.izr", mocSongs("Broke")),
      RsongAsset("Euphoria_Immersive.izr", s"$path/Songs/Euphoria_Immersive.izr", mocSongs("Euphoria")),
      RsongAsset("Euphoria_Stereo.izr", s"$path/Songs/Euphoria_Stereo.izr", mocSongs("Euphoria")),
      RsongAsset("Euphoria.jpg", s"$path/Labels/Euphoria.jpg", mocSongs("Euphoria")),
      RsongAsset("Tiny_Human_Immersive.izr", s"$path/Songs/Tiny_Human_Immersive.izr", mocSongs("Tiny_Human")),
      RsongAsset("Tiny_Human_Stereo.izr", s"$path/Songs/Tiny_Human_Stereo.izr", mocSongs("Tiny_Human")),
      RsongAsset("Tiny Human.jpg", s"$path/Labels/Tiny Human.jpg", mocSongs("Tiny_Human"))
    )
}
