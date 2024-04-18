package dev.toastbits.sinksabre.settings

fun Settings.Key.getName(): String =
    when (this) {
        Settings.Key.SYNC_ON_START -> "Sync on start"
        Settings.Key.SYNC_METHOD -> "Song sync method"
        Settings.Key.LOCAL_MAPS_PATH -> "Local maps path"
        Settings.Key.SCROLL_WARNING_DISMISSED -> "Scroll warning dismissed"
    }

fun Settings.Key.getDescription(): String? =
    when (this) {
        Settings.Key.SYNC_ON_START -> null
        Settings.Key.SYNC_METHOD -> null
        Settings.Key.LOCAL_MAPS_PATH -> null
        Settings.Key.SCROLL_WARNING_DISMISSED -> null
    }
