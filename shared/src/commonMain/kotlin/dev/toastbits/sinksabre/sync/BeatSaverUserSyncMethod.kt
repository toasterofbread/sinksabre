package dev.toastbits.sinksabre.sync

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.put
import kotlinx.serialization.json.addAll
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.encodeToString
import dev.toastbits.sinksabre.sync.beatsaver.*
import dev.toastbits.sinksabre.platform.AppContext
import dev.toastbits.sinksabre.ui.component.settingsfield.StringSettingsField
import dev.toastbits.sinksabre.settings.Settings
import dev.toastbits.composekit.platform.PlatformFile
import dev.toastbits.composekit.platform.PreferencesGroup
import dev.toastbits.composekit.platform.PreferencesProperty
import dev.toastbits.composekit.platform.PlatformPreferences
import dev.toatsbits.sinksabre.model.Song
import dev.toatsbits.sinksabre.model.LocalSong
import androidx.compose.runtime.*
import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import io.ktor.client.request.headers
import io.ktor.client.request.setBody
import io.ktor.client.request.post
import io.ktor.http.contentType
import io.ktor.http.ContentType

@Serializable
data class BeatSaverUserSyncMethod(
    val username: String = "",
    val password: String = ""
): SyncMethod {
    override fun getType(): SyncMethod.Type = SyncMethod.Type.BEAT_SAVER_USER

    override fun isConfigured(): Boolean = username.isNotBlank() && password.isNotBlank()

    @Composable
    override fun ConfigurationItems(context: AppContext, onModification: (SyncMethod) -> Unit) {
        StringSettingsField(
            remember(this) {
                object : PreferencesProperty<String> {
                    override fun get(): String =
                        username

                    override fun set(value: String, editor: PlatformPreferences.Editor?) {
                        onModification(copy(username = value))
                    }

                    override fun set(data: JsonElement, editor: PlatformPreferences.Editor?) {
                        set(data.jsonPrimitive.content, editor)
                    }

                    override val key: String = ""
                    override val name: String = "Username"
                    override val description: String? = null

                    override fun serialise(value: Any?): JsonElement =
                        JsonPrimitive(value as String?)

                    override fun getDefaultValue(): String = ""

                    override fun reset() {
                        set(getDefaultValue())
                    }

                    @Composable
                    override fun observe(): MutableState<String> {
                        val state: MutableState<String> = remember(this) { mutableStateOf(username) }
                        var set_to: String by remember(this) { mutableStateOf(username) }

                        LaunchedEffect(state.value) {
                            if (state.value != set_to) {
                                set_to = state.value
                                set(set_to)
                            }
                        }

                        return state
                    }
                }
            }
        )

        StringSettingsField(
            remember(this) {
                object : PreferencesProperty<String> {
                    override fun get(): String =
                        password

                    override fun set(value: String, editor: PlatformPreferences.Editor?) {
                        onModification(copy(password = value))
                    }

                    override fun set(data: JsonElement, editor: PlatformPreferences.Editor?) {
                        set(data.jsonPrimitive.content, editor)
                    }

                    override val key: String = ""
                    override val name: String = "Password"
                    override val description: String? = null

                    override fun serialise(value: Any?): JsonElement =
                        JsonPrimitive(value as String?)

                    override fun getDefaultValue(): String = ""

                    override fun reset() {
                        set(getDefaultValue())
                    }

                    @Composable
                    override fun observe(): MutableState<String> {
                        val state: MutableState<String> = remember(this) { mutableStateOf(password) }
                        var set_to: String by remember(this) { mutableStateOf(password) }

                        LaunchedEffect(state.value) {
                            if (state.value != set_to) {
                                set_to = state.value
                                set(set_to)
                            }
                        }

                        return state
                    }
                }
            },
            censorable = true
        )
    }

    private suspend fun HttpClient.getSession(): BeatSaverSession {
        session?.also {
            return it
        }

        getSession(username, password).also {
            session = it
            return it
        }
    }

    @kotlinx.serialization.Transient
    private var session: BeatSaverSession? = null

    override suspend fun getSongList(): Result<List<Song>> = runCatching {
        val client: HttpClient = getClient()
        val session: BeatSaverSession = client.getSession()

        val bookmarks_playlist: BeatSaverPlaylist = client.getBookmarksPlaylist(session)
        val maps: List<BeatSaverMap> = client.getPlaylistMaps(bookmarks_playlist.playlistId, session)

        return@runCatching maps.map { it.toSong() }
    }

    override suspend fun downloadSongs(
        directory: PlatformFile,
        onFractionalProgress: (Float?) -> Unit,
        onProgress: (String) -> Unit
    ): Result<List<LocalSong>> = runCatching {
        val client: HttpClient = getClient()

        onProgress("Getting user session")
        val session: BeatSaverSession = client.getSession()

        onProgress("Getting bookmarks playlist")
        val bookmarks_playlist: BeatSaverPlaylist = client.getBookmarksPlaylist(session)

        onProgress("Getting bookmarks")
        val all_maps: List<BeatSaverMap> = client.getPlaylistMaps(bookmarks_playlist.playlistId, session)

        val maps: List<BeatSaverMap> = all_maps.filter { map ->
            for (version in map.versions) {
                val files: List<PlatformFile> = directory.resolve(version.hash).listFiles() ?: emptyList()
                if (files.any { it.name.endsWith(".zip") } || files.isEmpty()) {
                    continue
                }

                return@filter false
            }

            return@filter true
        }

        onProgress("Downloading ${maps.size} maps (skipping ${all_maps.size - maps.size} already downloaded)")
        onFractionalProgress(0f)

        return@runCatching client.downloadBeatSaverMaps(
            maps,
            directory,
            onFractionalProgress,
            onProgress
        )
    }

    override fun canUploadSongs(): Boolean = true

    override suspend fun uploadSongs(
        songs: List<LocalSong>,
        onFractionalProgress: (Float?) -> Unit,
        onProgress: (String) -> Unit
    ): Result<Unit> = runCatching {
        val client: HttpClient = getClient()

        onProgress("Getting user session")
        val session: BeatSaverSession = client.getSession()

        onProgress("Getting bookmarks playlist")
        val bookmarks_playlist: BeatSaverPlaylist = client.getBookmarksPlaylist(session)

        onProgress("Adding maps to bookmarks")

        val hash_chunks: List<List<String>> = songs.map { it.hash }.chunked(100)

        for ((index, chunk) in hash_chunks.withIndex()) {
            onProgress("Adding maps to bookmarks (chunk ${index + 1} of ${hash_chunks.size})")

            val response: HttpResponse =
                client.post("https://beatsaver.com/api/playlists/id/${bookmarks_playlist.playlistId}/batch") {
                    headers {
                        append("Cookie", session.cookie)
                    }

                    contentType(ContentType.Application.Json)
                    setBody(Json.encodeToString(
                        buildJsonObject {
                            putJsonArray("hashes") {
                                addAll(chunk)
                            }
                            put("ignoreUnknown", true)
                            put("inPlaylist", true)
                            putJsonArray("keys") {}
                        }
                    ))
                }
        }
    }

    override fun toString(): String {
        val pw: String =
            if (password.isBlank()) "(blank)"
            else "***"
        return "BeatSaverUserSyncMethod(username=$username, password=$pw)"
    }
}
