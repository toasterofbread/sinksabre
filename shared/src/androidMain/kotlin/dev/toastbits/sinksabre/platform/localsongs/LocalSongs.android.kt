package dev.toastbits.sinksabre.platform.localsongs

import dev.toatsbits.sinksabre.model.LocalSong
import dev.toastbits.sinksabre.platform.AppContext
import dev.toastbits.sinksabre.sync.SyncMethod
import dev.toastbits.composekit.platform.PlatformFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.pm.PackageManager
import android.content.Context
import android.app.Activity

actual object LocalSongs {
    actual suspend fun getLocalSongs(context: AppContext): Result<List<LocalSong>?> = withContext(Dispatchers.IO) { runCatching {
        val ctx: Activity = context.activity

        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ctx, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PackageManager.PERMISSION_GRANTED)
            return@runCatching null
        }

        val directory: PlatformFile = getMapsDirectory(context)
        return@runCatching loadLocalSongsInDirectory(directory)
    } }

    actual suspend fun downloadToLocalSongs(
        method: SyncMethod,
        context: AppContext,
        onFractionalProgress: (Float?) -> Unit,
        onProgress: (String) -> Unit
    ): Result<List<LocalSong>> {
        if (ContextCompat.checkSelfPermission(context.activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context.activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PackageManager.PERMISSION_GRANTED)
            throw RuntimeException("Storage write permission not granted")
        }

        return method.downloadSongs(getMapsDirectory(context), onFractionalProgress, onProgress)
    }
}
