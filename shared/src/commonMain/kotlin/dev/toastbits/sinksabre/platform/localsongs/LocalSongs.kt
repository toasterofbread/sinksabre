package dev.toastbits.sinksabre.platform.localsongs

import dev.toatsbits.sinksabre.model.LocalSong

expect object LocalSongs {
    suspend fun getLocalSongs(): List<LocalSong>
}
