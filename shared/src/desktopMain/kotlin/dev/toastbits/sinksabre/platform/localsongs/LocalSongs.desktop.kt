package dev.toastbits.sinksabre.platform.localsongs

import dev.toatsbits.sinksabre.model.LocalSong
import dev.toastbits.sinksabre.platform.AppContext
import dev.toastbits.sinksabre.sync.SyncMethod
import dev.toastbits.composekit.platform.PlatformFile

actual object LocalSongs {
    private fun getLevelsDirectory(context: AppContext): PlatformFile =
        PlatformFile(context.getFilesDir().resolve("maps"))

    actual suspend fun getLocalSongs(context: AppContext): Result<List<LocalSong>?> = runCatching {
        val directory: PlatformFile = getLevelsDirectory(context)
        return@runCatching loadLocalSongsInDirectory(directory)
    }

    actual suspend fun downloadToLocalSongs(
        method: SyncMethod,
        context: AppContext,
        onFractionalProgress: (Float?) -> Unit,
        onProgress: (String) -> Unit
    ): Result<List<LocalSong>> {
        return method.downloadSongs(getLevelsDirectory(context), onFractionalProgress, onProgress)
    }
}
