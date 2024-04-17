package dev.toastbits.sinksabre.platform

import dev.toastbits.composekit.platform.PlatformContext
import dev.toastbits.composekit.platform.PlatformPreferences
import dev.toastbits.composekit.platform.PlatformPreferencesImpl
import android.content.Context
import android.content.Intent
import android.content.ComponentName
import android.app.Activity
import kotlinx.coroutines.CoroutineScope
import android.net.Uri

actual class AppContext(
    val activity: Activity,
    coroutine_scope: CoroutineScope
): PlatformContext(activity, coroutine_scope) {
    actual fun getPrefs(): PlatformPreferences = PlatformPreferencesImpl.getInstance(ctx)

    actual fun canLaunchBeatSaber(): Boolean = true
    actual fun launchBeatSaber(): Boolean {
        // Why does this work for BMBF2 but not this?
        val launch_intent: Intent = Intent()
        launch_intent.setComponent(ComponentName("com.beatgames.beatsaber", "com.unity3d.player.UnityPlayerActivity"))
        activity.startActivity(launch_intent)
        return true
    }
}
