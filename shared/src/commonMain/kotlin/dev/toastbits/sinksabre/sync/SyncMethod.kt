package dev.toastbits.sinksabre.sync

import androidx.compose.runtime.Composable
import dev.toastbits.sinksabre.platform.AppContext
import dev.toastbits.composekit.platform.PlatformFile
import dev.toatsbits.sinksabre.model.Song
import dev.toatsbits.sinksabre.model.LocalSong
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

@Serializable
sealed interface SyncMethod {
    fun getType(): Type
    fun isConfigured(): Boolean

    @Composable
    fun ConfigurationItems(context: AppContext, onModification: (SyncMethod) -> Unit)

    suspend fun getSongList(): Result<List<Song>>

    suspend fun downloadSongs(
        directory: PlatformFile,
        onFractionalProgress: (Float?) -> Unit = {},
        onProgress: (String) -> Unit
    ): Result<List<LocalSong>>

    fun canUploadSongs(): Boolean = false
    suspend fun uploadSongs(
        songs: List<LocalSong>,
        onFractionalProgress: (Float?) -> Unit = {},
        onProgress: (String) -> Unit
    ): Result<Unit> = Result.failure(IllegalAccessException())

    enum class Type {
        BEAT_SAVER_USER,
        BEAT_SAVER_PLAYLIST;

        fun create(): SyncMethod =
            when (this) {
                BEAT_SAVER_USER -> BeatSaverUserSyncMethod()
                BEAT_SAVER_PLAYLIST -> BeatSaverPlaylistSyncMethod()
            }
    }

    suspend fun getClient(): HttpClient =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        explicitNulls = false
                    }
                )
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 10000
            }
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 5)
                exponentialDelay()
            }
        }
}

fun SyncMethod.Type?.getName(): String =
    when (this) {
        SyncMethod.Type.BEAT_SAVER_USER -> "BeatSaver user"
        SyncMethod.Type.BEAT_SAVER_PLAYLIST -> "BeatSaver playlist"
        null -> "None"
    }
