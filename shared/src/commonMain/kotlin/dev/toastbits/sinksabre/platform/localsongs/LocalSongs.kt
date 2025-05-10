package dev.toastbits.sinksabre.platform.localsongs

import dev.toatsbits.sinksabre.model.LocalSong
import dev.toastbits.sinksabre.platform.AppContext
import dev.toastbits.sinksabre.sync.SyncMethod
import dev.toastbits.sinksabre.settings.settings
import dev.toastbits.composekit.platform.PlatformFile
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.ExperimentalSerializationApi
import java.io.File

expect object LocalSongs {
    suspend fun getLocalSongs(context: AppContext): Result<List<LocalSong>?>

    suspend fun downloadToLocalSongs(
        method: SyncMethod,
        context: AppContext,
        onFractionalProgress: (Float?) -> Unit = {},
        onProgress: (String) -> Unit
    ): Result<List<LocalSong>>
}

fun getMapsDirectory(context: AppContext): PlatformFile {
    val file: File = File(context.settings.LOCAL_MAPS_PATH.get())
    file.mkdirs()
    return PlatformFile.fromFile(
        file,
        context
    )
}

fun loadLocalSongFile(file: PlatformFile): LocalSong? {
    if (!file.is_directory) {
        return null
    }

    val info_file: PlatformFile? = file.listFiles()?.firstOrNull { it.name.lowercase() == "info.dat" }
    if (info_file?.is_file != true) {
        return null
    }

    val info_text: String =
        info_file.inputStream().reader().use {
            it.readText()
        }

    return SongInfoData.fromString(info_text).toLocalSong(file)
}

suspend fun loadLocalSongsInDirectory(directory: PlatformFile): List<LocalSong> {
    val files: List<PlatformFile> = directory.listFiles() ?: emptyList()
    val semaphore: Semaphore = Semaphore(5)

    val songs: Array<LocalSong?> = arrayOfNulls(files.size)

    coroutineScope {
        for ((index, file) in files.withIndex()) {
            launch(Dispatchers.IO) {
                semaphore.withPermit {
                    songs[index] = loadLocalSongFile(file)
                }
            }
        }
    }

    return songs.filterNotNull()
}

interface SongInfoData {
    fun toLocalSong(song_directory: PlatformFile): LocalSong

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        private val json: Json =
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
                allowTrailingComma = true
            }

        fun fromString(string: String): SongInfoData {
            val version: List<String> = getVersion(string).split(".")
            try {
                return when (version.first()) {
                    "4" -> json.decodeFromString<SongInfoDatav4>(string)
                    else -> json.decodeFromString<SongInfoDatav2>(string)
                }
            }
            catch (e: Throwable) {
                throw RuntimeException("Parsing SongInfoData with version $version from '$string' failed", e)
            }
        }

        @Serializable
        private data class VersionInfo(
            val version: String? = null,
            val _version: String? = null
        )

        private fun getVersion(string: String): String =
            json.decodeFromString<VersionInfo>(string).let { (version, _version) ->
                version ?: _version ?: throw NotImplementedError("Version string not found in '$string'")
            }
    }
}

@Serializable
private data class SongInfoDatav2(
    val _songName: String,
    val _songSubName: String,
    val _songAuthorName: String,
    val _levelAuthorName: String,
    val _beatsPerMinute: Float,
    val _songFilename: String,
    val _coverImageFilename: String
): SongInfoData {
    override fun toLocalSong(song_directory: PlatformFile): LocalSong =
        LocalSong(
            hash = song_directory.name,
            name = _songName.filledOrNull(),
            subname = _songSubName.filledOrNull(),
            artist_name = _songAuthorName.filledOrNull(),
            mapper_name = _levelAuthorName.filledOrNull(),
            bpm = _beatsPerMinute,
            image_file = _coverImageFilename.filledOrNull()?.let { song_directory.resolve(it) },
            audio_file = _songFilename.filledOrNull()?.let { song_directory.resolve(it) }
        )

    private fun String.filledOrNull(): String? =
        if (isBlank()) null
        else this
}

@Serializable
private data class SongInfoDatav4(
    val song: Song,
    val audio: Audio,
    val coverImageFilename: String
): SongInfoData {
    @Serializable
    data class Song(
        val title: String,
        val subTitle: String,
        val author: String
    )

    @Serializable
    data class Audio(
        val bpm: Float,
        val songFilename: String
    )

    override fun toLocalSong(song_directory: PlatformFile): LocalSong =
        SongInfoDatav2(
            _songName = song.title,
            _songSubName = song.subTitle,
            _songAuthorName = song.author,
            _levelAuthorName = "Unknown", // TODO
            _beatsPerMinute = audio.bpm,
            _songFilename = audio.songFilename,
            _coverImageFilename = coverImageFilename
        ).toLocalSong(song_directory)
}
