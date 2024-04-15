package dev.toastbits.sinksabre.platform.localsongs

import dev.toatsbits.sinksabre.model.LocalSong

actual object LocalSongs {
    actual suspend fun getLocalSongs(): List<LocalSong> {
        return listOf(LocalSong("Hello World!"))
    }
}
