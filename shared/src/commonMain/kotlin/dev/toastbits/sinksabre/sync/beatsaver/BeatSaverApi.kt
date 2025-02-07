package dev.toastbits.sinksabre.sync.beatsaver

import dev.toastbits.composekit.platform.PlatformFile
import dev.toastbits.sinksabre.platform.localsongs.SongInfoData
import dev.toatsbits.sinksabre.model.LocalSong
import dev.toatsbits.sinksabre.model.BeatSaverSong
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.util.zip.ZipInputStream
import java.util.zip.ZipEntry
import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readBytes
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.headers
import io.ktor.client.request.get
import io.ktor.client.call.body
import io.ktor.http.contentType
import io.ktor.http.ContentType

suspend fun HttpClient.downloadBeatSaverMaps(
    maps: List<BeatSaverMap>,
    directory: PlatformFile,
    onFractionalProgress: (Float?) -> Unit,
    onProgress: (String) -> Unit
): List<LocalSong> {
    val added_songs: MutableList<LocalSong> = mutableListOf()
    val semaphore: Semaphore = Semaphore(3)

    coroutineScope {
        for ((index, map) in maps.withIndex()) {
            launch(Dispatchers.IO) {
                semaphore.withPermit {
                    val progress_message_map: String = "${map.name} by ${map.uploader.name} (${index + 1} / ${maps.size})"
                    onProgress("Downloading $progress_message_map")

                    val version: BeatSaverSong.Version = BeatSaverSong.selectVersion(map.versions.map { it.toSongVersion() })

                    val map_dir: PlatformFile = directory.resolve(version.hash)

                    check(map_dir.mkdirs()) { "Could not create map directory at '${map_dir.absolute_path}'" }

                    val zip_file: PlatformFile = map_dir.resolve("${version.hash}.zip")

                    val zip_data: ByteArray = get(version.download_url).readBytes()
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

    return added_songs
}

suspend fun HttpClient.getSession(username: String, password: String): BeatSaverSession {
    val login_response: HttpResponse = post("https://beatsaver.com/login") {
        contentType(ContentType.Application.FormUrlEncoded)
        setBody("username=${username}&password=${password}")

        headers {
            append("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
            append("Accept-Encoding", "gzip,deflate,br")
        }
    }

    val cookie: String = login_response.headers.get("set-cookie") ?: throw RuntimeException("BeatSaverSession cookie not provided by server")

    val index_response: HttpResponse = get("https://beatsaver.com/") {
        headers {
            append("Cookie", cookie)
        }
    }

    val index_body: String = index_response.bodyAsText()

    val user_id_key: String = "&quot;userId&quot;:"

    val user_id_index: Int = index_body.indexOf(user_id_key) + user_id_key.length
    check(user_id_index != user_id_key.length - 1) { index_body }

    val user_id_section: String = index_body.substring(user_id_index).trimStart()

    val user_id_end: Int = user_id_section.indexOfFirst { !it.isDigit() }
    check(user_id_end != -1) { user_id_section }

    val user_id: Int = user_id_section.substring(0, user_id_end).trimEnd().toInt()

    return BeatSaverSession(
        cookie = cookie,
        user_id = user_id
    )
}

suspend fun HttpClient.getBookmarksPlaylist(session: BeatSaverSession): BeatSaverPlaylist {
    val playlists_result: BeatSaverUserPlaylistsResponse =
        get("https://beatsaver.com/api/playlists/user/${session.user_id}/0") {
            headers {
                append("Cookie", session.cookie)
            }
        }.body()

    return playlists_result.docs.firstOrNull { it.isSystemPlaylist() }
        ?: throw RuntimeException("Bookmarks playlist not found")
}

suspend fun HttpClient.getPlaylistMaps(playlist_id: Int, session: BeatSaverSession?): List<BeatSaverMap> {
    val maps: MutableList<BeatSaverMap> = mutableListOf()
    var playlist_size: Int
    var page: Int = 0

    do {
        val bookmarks_result: BeatSaverPlaylistResponse = getPlaylistPage(playlist_id, page++, session)
        playlist_size = bookmarks_result.playlist.stats.totalMaps

        for (map in bookmarks_result.maps) {
            maps.add(map.map)
        }
    }
    while (maps.size < playlist_size)

    return maps.filter { it.versions.isNotEmpty() }
}

suspend fun HttpClient.getPlaylistPage(playlist_id: Int, page: Int, session: BeatSaverSession?): BeatSaverPlaylistResponse {
    return get("https://beatsaver.com/api/playlists/id/$playlist_id/$page") {
        if (session != null) {
            headers {
                append("Cookie", session.cookie)
            }
        }
    }.body()
}
