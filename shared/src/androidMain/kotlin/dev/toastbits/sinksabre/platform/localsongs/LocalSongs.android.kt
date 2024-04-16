package dev.toastbits.sinksabre.platform.localsongs

import dev.toatsbits.sinksabre.model.LocalSong
import dev.toastbits.sinksabre.platform.AppContext
import dev.toastbits.composekit.platform.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.pm.PackageManager
import android.content.Context
import android.app.Activity

actual object LocalSongs {
    private fun getLevelsDirectory(context: AppContext): PlatformFile =
        PlatformFile.fromFile(
            File("/storage/emulated/0/ModData/com.beatgames.beatsaber/Mods/SongLoader/CustomLevels"),
            context
        )

    actual suspend fun getLocalSongs(context: AppContext): Result<List<LocalSong>?> = withContext(Dispatchers.IO) { runCatching {
        val ctx: Activity = context.activity

        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ctx, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PackageManager.PERMISSION_GRANTED)
            return@runCatching null
        }

        val directory: PlatformFile = getLevelsDirectory(context)
        val levels: List<PlatformFile> = directory.listFiles() ?: emptyList()

        return@runCatching levels.mapNotNull { level ->
            if (!level.is_directory) {
                return@mapNotNull null
            }

            val info_file: PlatformFile = level.resolve("Info.dat")
            if (!info_file.is_file) {
                return@mapNotNull null
            }

            val info_text: String =
                info_file.inputStream().reader().use {
                    it.readText()
                }

            val song_info: SongInfo = Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            }.decodeFromString(info_text)

            return@mapNotNull with (song_info) {
                LocalSong(
                    id = level.name,
                    name = _songName.fullOrNull(),
                    subname = _songSubName.fullOrNull(),
                    artist_name = _songAuthorName.fullOrNull(),
                    mapper_name = _levelAuthorName.fullOrNull(),
                    bpm = _beatsPerMinute,
                    image_file = _coverImageFilename.fullOrNull()?.let { level.resolve(it) },
                    audio_file = _songFilename.fullOrNull()?.let { level.resolve(it) }
                )
            }
        }
    } }
}

private fun String.fullOrNull(): String? =
    if (isBlank()) null
    else this

@Serializable
private data class SongInfo(
    val _songName: String,
    val _songSubName: String,
    val _songAuthorName: String,
    val _levelAuthorName: String,
    val _beatsPerMinute: Float,
    val _songFilename: String,
    val _coverImageFilename: String
)
