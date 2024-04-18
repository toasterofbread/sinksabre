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
import java.util.Timer

actual class AppContext(
    val activity: Activity,
    coroutine_scope: CoroutineScope
): PlatformContext(activity, coroutine_scope) {
    actual fun getPrefs(): PlatformPreferences = PlatformPreferencesImpl.getInstance(ctx)

    actual fun canLaunchBeatSaber(): Boolean = true
    actual fun launchBeatSaber(): Boolean {
        activity.finish()
        
        val launch_intent: Intent = activity.getPackageManager().getLaunchIntentForPackage("com.beatgames.beatsaber")

        launch_intent.setFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
            Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
            Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )

        Timer().schedule(650) {
            activity.startActivity(launch_intent)
        }
        Timer().schedule(800) {
            activity.startActivity(launch_intent)
        }

        return true
    }
}
