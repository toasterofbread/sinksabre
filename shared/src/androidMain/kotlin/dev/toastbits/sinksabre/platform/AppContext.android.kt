package dev.toastbits.sinksabre.platform

import dev.toastbits.composekit.platform.PlatformContext
import dev.toastbits.composekit.platform.PlatformPreferences
import dev.toastbits.composekit.platform.PlatformPreferencesImpl
import android.content.Context
import android.app.Activity
import kotlinx.coroutines.CoroutineScope

actual class AppContext(val activity: Activity, coroutine_scope: CoroutineScope): PlatformContext(activity, coroutine_scope) {
    actual fun getPrefs(): PlatformPreferences = PlatformPreferencesImpl.getInstance(ctx)

    actual fun launchBeatSaber(): Boolean {
        val launch_intent: Intent = activity.package_manager.getLaunchIntentForPackage("com.beatgames.beatsaber") ?: return false
        activity.startActivity(launch_intent)
        return true
    }
}
