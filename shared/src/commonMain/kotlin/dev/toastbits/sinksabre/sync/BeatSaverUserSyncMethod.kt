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
import dev.toastbits.sinksabre.platform.localsongs.SongInfoData
import dev.toastbits.composekit.utils.composable.OnChangedEffect
import dev.toastbits.composekit.platform.PlatformFile
import dev.toatsbits.sinksabre.model.Song
import dev.toatsbits.sinksabre.model.BeatSaverSong
import dev.toatsbits.sinksabre.model.LocalSong
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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.addAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope

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

    override suspend fun getSongList(): Result<List<Song>> = runCatching {
        val client: HttpClient = getClient()

        val bookmarks_playlist: BeatSaverPlaylist = client.getBookmarksPlaylist()
        val maps: List<BeatSaverMap> = client.getPlaylistMaps(bookmarks_playlist)

        return@runCatching maps.map { it.toSong() }
    }

    override suspend fun downloadSongs(
        directory: PlatformFile,
        onFractionalProgress: (Float?) -> Unit,
        onProgress: (String) -> Unit
    ): Result<List<LocalSong>> = runCatching {
        val client: HttpClient = getClient()

        onProgress("Getting user session")
        val session: Session = client.getSession()

        onProgress("Getting bookmarks playlist")
        val bookmarks_playlist: BeatSaverPlaylist = client.getBookmarksPlaylist()

        onProgress("Getting bookmarks")
        val all_maps: List<BeatSaverMap> = client.getPlaylistMaps(bookmarks_playlist)

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

        val added_songs: MutableList<LocalSong> = mutableListOf()

        val semaphore: Semaphore = Semaphore(3)

        coroutineScope {
            for ((index, map) in maps.withIndex()) {
                launch(Dispatchers.IO) {
                    semaphore.withPermit {
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

                        var info_dat: String? = null

                        onProgress("Extracting $progress_message_map")
                        ZipInputStream(zip_file.inputStream()).use { zip_input ->
                            var entry: ZipEntry? = zip_input.nextEntry
                            while (entry != null) {
                                val entry_file: PlatformFile = map_dir.resolve(entry.name.lowercase())
                                if (entry.isDirectory) {
                                    entry_file.mkdirs()
                                }
                                else {
                                    entry_file.outputStream().use { entry_output ->
                                        zip_input.copyTo(entry_output)
                                    }

                                    if (entry_file.name == "info.dat") {
                                        entry_file.inputStream().reader().use {
                                            info_dat = it.readText()
                                        }
                                    }
                                }

                                entry = zip_input.nextEntry
                            }
                        }

                        val song_info: SongInfoData? = info_dat?.let { SongInfoData.fromString(it) }

                        if (song_info != null) {
                            added_songs.add(song_info.toLocalSong(map_dir))
                        }
                        else {
                            added_songs.add(LocalSong(hash = version.hash))
                        }

                        zip_file.delete()

                        onFractionalProgress(added_songs.size / maps.size.toFloat())
                    }
                }
            }
        }

        return@runCatching added_songs
    }

    override fun canUploadSongs(): Boolean = true

    override suspend fun uploadSongs(
        songs: List<LocalSong>,
        onFractionalProgress: (Float?) -> Unit,
        onProgress: (String) -> Unit
    ): Result<Unit> = runCatching {
        val client: HttpClient = getClient()

        onProgress("Getting user session")
        val session: Session = client.getSession()

        onProgress("Getting bookmarks playlist")
        val bookmarks_playlist: BeatSaverPlaylist = client.getBookmarksPlaylist()

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

    private suspend fun HttpClient.getBookmarksPlaylist(): BeatSaverPlaylist {
        val session: Session = getSession()

        val playlists_result: BeatSaverUserPlaylistsResponse =
            get("https://beatsaver.com/api/playlists/user/${session.user_id}/0") {
                headers {
                    append("Cookie", session.cookie)
                }
            }.body()

        return playlists_result.docs.firstOrNull { it.isSystemPlaylist() }
            ?: throw RuntimeException("Bookmarks playlist not found")
    }

    private suspend fun HttpClient.getPlaylistMaps(playlist: BeatSaverPlaylist): List<BeatSaverMap> {
        val session: Session = getSession()

        val bookmarks_playlist: BeatSaverPlaylist = getBookmarksPlaylist()

        val maps: MutableList<BeatSaverMap> = mutableListOf()
        var playlist_size: Int
        var page: Int = 0

        do {
            val bookmarks_result: BeatSaverPlaylistResponse = getPlaylistPage(bookmarks_playlist, page++)
            playlist_size = bookmarks_result.playlist.stats.totalMaps

            for (map in bookmarks_result.maps) {
                maps.add(map.map)
            }
        }
        while (maps.size < playlist_size)

        return maps.filter { it.versions.isNotEmpty() }
    }

    private suspend fun HttpClient.getPlaylistPage(playlist: BeatSaverPlaylist, page: Int): BeatSaverPlaylistResponse {
        val session: Session = getSession()
        return get("https://beatsaver.com/api/playlists/id/${playlist.playlistId}/$page") {
            headers {
                append("Cookie", session.cookie)
            }
        }.body()
    }
}

@Serializable
private data class BeatSaverUserPlaylistsResponse(val docs: List<BeatSaverPlaylist>)

@Serializable
private data class BeatSaverPlaylistResponse(
    val playlist: BeatSaverPlaylist,
    val maps: List<MapContainer>
) {
    @Serializable
    data class MapContainer(val map: BeatSaverMap)
}

@Serializable
private data class BeatSaverPlaylist(
    val playlistId: Int,
    val type: String,
    val stats: Stats
) {
    fun isSystemPlaylist(): Boolean = type == "System"

    @Serializable
    data class Stats(val totalMaps: Int)
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
    val versions: List<Version>,
    val metadata: Metadata
) {
    @Serializable
    data class Version(
        val hash: String,
        val downloadURL: String,
        val coverURL: String,
        val previewURL: String
    )

    @Serializable
    data class Metadata(val bpm: Float)

    fun toSong(): BeatSaverSong =
        BeatSaverSong(
            id = id,
            versions.map { version ->
                BeatSaverSong.Version(
                    hash = version.hash,
                    download_url = version.downloadURL,
                    image_url = version.coverURL,
                    preview_url = version.previewURL
                )
            },
            name = name,
            mapper_name = uploader.name,
            bpm = metadata.bpm
        )
}
