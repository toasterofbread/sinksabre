package dev.toastbits.sinksabre.settings

fun Settings.Key.getName(): String =
    when (this) {
        Settings.Key.SYNC_ON_START -> "Sync on start"
        Settings.Key.SYNC_METHOD -> "Song sync method"
    }

fun Settings.Key.getDescription(): String? =
    when (this) {
        Settings.Key.SYNC_ON_START -> null
        Settings.Key.SYNC_METHOD -> null
    }
