package dev.toastbits.sinksabre.sync

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import io.ktor.client.call.body
import io.ktor.client.request.get
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import dev.toastbits.sinksabre.ui.component.settingsfield.StringSettingsField
import dev.toastbits.sinksabre.platform.AppContext
import dev.toastbits.composekit.utils.composable.OnChangedEffect
import dev.toastbits.composekit.platform.PlatformFile
import dev.toatsbits.sinksabre.model.Song
import dev.toastbits.sinksabre.settings.Settings
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readBytes
import io.ktor.client.request.headers
import io.ktor.utils.io.copyAndClose
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.io.File

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
                object : Settings.Field<String> {
                    override fun get(): String = username
                    override fun set(value: String) = onModification(copy(username = value))

                    override fun getName(): String = "Username"
                    override fun getDescription(): String? = null

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
                object : Settings.Field<String> {
                    override fun get(): String = password
                    override fun set(value: String) = onModification(copy(password = value))

                    override fun getName(): String = "Password"
                    override fun getDescription(): String? = null

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

    private data class Session(val cookie: String, val user_id: Int)
    @kotlinx.serialization.Transient
    private var session: Session? = null

    private suspend fun HttpClient.getSession(): Session {
        session?.also {
            return it
        }

        val login_response: HttpResponse = post("https://beatsaver.com/login") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody("username=${username}&password=${password}")

            headers {
                append("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                append("Accept-Encoding", "gzip,deflate,br")
            }
        }

        val cookie: String = login_response.headers.get("set-cookie") ?: throw RuntimeException("Session cookie not provided by server")

        val index_response: HttpResponse = get("https://beatsaver.com/") {
            headers {
                append("Cookie", cookie)
            }
        }

        val index_body: String = index_response.bodyAsText()

        val user_id_key: String = "&quot;userId&quot;: "
        val user_id_index: Int = index_body.indexOf(user_id_key) + user_id_key.length

        val user_id: Int = index_body.substring(user_id_index, index_body.indexOf(",", user_id_index)).toInt()

        session = Session(
            cookie = cookie,
            user_id = user_id
        )

        return session!!
    }

    override suspend fun downloadSongs(
        directory: PlatformFile,
        onProgress: (String) -> Unit
    ): Result<List<PlatformFile>> = runCatching {
        val client: HttpClient = getClient()

        onProgress("Getting user session")
        val session: Session = client.getSession()

        onProgress("Getting playlists")
        val playlists_result: BeatSaverUserPlaylistsResponse =
            client.get("https://beatsaver.com/api/playlists/user/${session.user_id}/0") {
                headers {
                    append("Cookie", session.cookie)
                }
            }.body()

        val bookmarks_playlist: BeatSaverPlaylist =
            playlists_result.docs.firstOrNull { it.isSystemPlaylist() }
            ?: throw RuntimeException("Bookmarks playlist not found")

        onProgress("Getting bookmarks")
        val bookmarks_result: BeatSaverPlaylistResponse =
            client.get("https://beatsaver.com/api/playlists/id/${bookmarks_playlist.playlistId}/0") {
                headers {
                    append("Cookie", session.cookie)
                }
            }.body()

        val maps: List<BeatSaverMap> =
            bookmarks_result.maps.mapNotNull {
                val map: BeatSaverMap = it.map
                val version: BeatSaverMap.Version =
                    map.versions.firstOrNull()
                    ?: return@mapNotNull null

                if (directory.resolve(version.hash).exists) {
                    return@mapNotNull null
                }
                return@mapNotNull map
            }

        onProgress("Downloading ${maps.size} maps")

        val added_maps: MutableList<PlatformFile> = mutableListOf()

        for ((index, map) in maps.withIndex()) {
            val progress_message_map: String = "${map.name} by ${map.uploader.name} (${index + 1} / ${maps.size})"
            onProgress("Downloading $progress_message_map")

            val version: BeatSaverMap.Version = map.versions.first()

            val map_dir: PlatformFile = directory.resolve(version.hash)

            check(map_dir.mkdirs()) { "Could not create map directory at '${map_dir.absolute_path}'" }

            val zip_file: PlatformFile = map_dir.resolve("${version.hash}.zip")

            val zip_data: ByteArray = client.get(version.downloadURL).readBytes()
            zip_file.outputStream().use { zip_output ->
                zip_output.write(zip_data)
            }

            onProgress("Extracting $progress_message_map")
            ZipInputStream(zip_file.inputStream()).use { zip_input ->
                var entry: ZipEntry? = zip_input.nextEntry
                while (entry != null) {
                    val entry_file: PlatformFile = map_dir.resolve(entry.name)
                    if (entry.isDirectory) {
                        entry_file.mkdirs()
                    }
                    else {
                        entry_file.outputStream().use { entry_output ->
                            zip_input.copyTo(entry_output)
                        }
                    }
                    entry = zip_input.nextEntry
                }
            }

            added_maps.add(map_dir)
            zip_file.delete()
        }

        return@runCatching added_maps
    }

    override fun toString(): String {
        val pw: String =
            if (password.isBlank()) "(blank)"
            else "***"
        return "BeatSaverUserSyncMethod(username=$username, password=$pw)"
    }
}

@Serializable
private data class BeatSaverUserPlaylistsResponse(val docs: List<BeatSaverPlaylist>)

@Serializable
private data class BeatSaverPlaylistResponse(val playlist: BeatSaverPlaylist, val maps: List<MapContainer>) {
    @Serializable
    data class MapContainer(val map: BeatSaverMap)
}

@Serializable
private data class BeatSaverPlaylist(
    val playlistId: Int,
    val type: String
) {
    fun isSystemPlaylist(): Boolean = type == "System"
}

@Serializable
private data class BeatSaverUserResponse(
    val id: Int,
    val name: String,
    val avatar: String
)

@Serializable
private data class BeatSaverMap(
    val id: String,
    val name: String,
    val description: String,
    val uploader: BeatSaverUserResponse,
    val versions: List<Version>
) {
    @Serializable
    data class Version(val hash: String, val downloadURL: String)
}
