package dev.toastbits.sinksabre.sync.beatsaver

import kotlinx.serialization.Serializable
import dev.toatsbits.sinksabre.model.BeatSaverSong

data class BeatSaverSession(val cookie: String, val user_id: Int)

@Serializable
data class BeatSaverUserPlaylistsResponse(val docs: List<BeatSaverPlaylist>)

@Serializable
data class BeatSaverPlaylistResponse(
    val playlist: BeatSaverPlaylist,
    val maps: List<MapContainer>
) {
    @Serializable
    data class MapContainer(val map: BeatSaverMap)
}

@Serializable
data class BeatSaverPlaylist(
    val playlistId: Int,
    val type: String,
    val stats: Stats
) {
    fun isSystemPlaylist(): Boolean = type == "System"

    @Serializable
    data class Stats(val totalMaps: Int)
}

@Serializable
data class BeatSaverUserResponse(
    val id: Int,
    val name: String,
    val avatar: String
)

@Serializable
data class BeatSaverMap(
    val id: String,
    val name: String,
    val description: String,
    val uploader: BeatSaverUserResponse,
    val versions: List<Version>,
    val metadata: Metadata
) {
    @Serializable
    data class Version(
        val hash: String,
        val downloadURL: String,
        val coverURL: String,
        val previewURL: String
    ) {
        fun toSongVersion(): BeatSaverSong.Version =
            BeatSaverSong.Version(
                hash = hash,
                download_url = downloadURL,
                image_url = coverURL,
                preview_url = previewURL
            )
    }

    @Serializable
    data class Metadata(val bpm: Float)

    fun toSong(): BeatSaverSong =
        BeatSaverSong(
            id = id,
            versions.map { it.toSongVersion() },
            name = name,
            mapper_name = uploader.name,
            bpm = metadata.bpm
        )
}
