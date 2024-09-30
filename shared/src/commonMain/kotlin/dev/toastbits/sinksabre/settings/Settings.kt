package dev.toastbits.sinksabre.settings

import dev.toastbits.composekit.platform.PreferencesGroup
import dev.toastbits.composekit.platform.PreferencesProperty
import dev.toastbits.composekit.platform.Platform
import dev.toastbits.sinksabre.platform.AppContext
import dev.toastbits.sinksabre.sync.SyncMethod

val AppContext.settings: Settings
    get() = Settings(this)

@Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
class Settings(val context: AppContext): PreferencesGroup(null, context.getPrefs()) {
    val SYNC_ON_START: PreferencesProperty<Boolean> by property(
        getName = { "Sync on start" },
        getDescription = { null },
        getDefaultValue = { false }
    )

    val SYNC_METHOD: PreferencesProperty<SyncMethod?> by nullableSerialisableProperty(
        getName = { "Song sync method" },
        getDescription = { null },
        getDefaultValue = { null }
    )

    val LOCAL_MAPS_PATH: PreferencesProperty<String> by property(
        getName = { "Local maps path" },
        getDescription = { null },
        getDefaultValue = {
            if (context.isRunningOnQuest()) "/storage/emulated/0/ModData/com.beatgames.beatsaber/Mods/SongCore/CustomLevels"
            else when (Platform.current) {
                Platform.ANDROID -> "/storage/emulated/0/BeatSaberMaps"
                Platform.DESKTOP -> context.getFilesDir().resolve("maps").absolutePath
            }
        }
    )

    val SCROLL_WARNING_DISMISSED: PreferencesProperty<Boolean> by property(
        getName = { "Scroll warning dismissed" },
        getDescription = { null },
        getDefaultValue = { !context.isRunningOnQuest() }
    )
}
