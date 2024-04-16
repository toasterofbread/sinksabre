package dev.toastbits.sinksabre.platform

import dev.toastbits.composekit.platform.PlatformContext
import dev.toastbits.composekit.platform.PlatformPreferences
import dev.toastbits.composekit.platform.PlatformPreferencesImpl

actual class AppContext: PlatformContext("SinkSabre", Object::class.java) {
    actual fun getPrefs(): PlatformPreferences = PlatformPreferencesImpl.getInstance { getFilesDir().resolve("preferences.json") }
}