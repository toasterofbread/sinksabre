package dev.toastbits.sinksabre.platform

import dev.toastbits.composekit.platform.PlatformContext
import dev.toastbits.composekit.platform.PlatformPreferences

expect class AppContext: PlatformContext {
    fun getPrefs(): PlatformPreferences

    fun canLaunchBeatSaber(): Boolean
    fun launchBeatSaber(): Boolean
}
