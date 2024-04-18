package dev.toastbits.sinksabre.settings

import dev.toastbits.sinksabre.platform.AppContext
import dev.toastbits.composekit.platform.PlatformPreferences
import dev.toastbits.composekit.platform.PlatformPreferencesListener
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import dev.toastbits.composekit.utils.composable.OnChangedEffect

val AppContext.settings: Settings
    get() = Settings(this)

@Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
class Settings(val context: AppContext) {
    val prefs: PlatformPreferences get() = context.getPrefs()

    val SYNC_ON_START: Field<Boolean> get() = PrefsField(Key.SYNC_ON_START)
    val SYNC_METHOD: Field<String> get() = PrefsField(Key.SYNC_METHOD)
    val SCROLL_WARNING_DISMISSED: Field<Boolean> get() = PrefsField(Key.SCROLL_WARNING_DISMISSED)

    interface Field<T> {
        fun get(): T
        fun set(value: T)

        @Composable
        fun observe(): MutableState<T>

        fun getName(): String
        fun getDescription(): String?
    }

    inner class PrefsField<T>(val key: Key): Field<T> {
        val prefs: PlatformPreferences get() = context.getPrefs()

        override fun get(): T = get(key, prefs)
        override fun set(value: T) = set(key, value, prefs)

        @Composable
        override fun observe(): MutableState<T> = observeSettingsKey()

        override fun getName(): String = key.getName()
        override fun getDescription(): String? = key.getDescription()
    }

    enum class Key {
        SYNC_ON_START,
        SYNC_METHOD,
        SCROLL_WARNING_DISMISSED;

        fun getDefaultValue(context: AppContext): Any =
            when (this) {
                SYNC_ON_START -> false
                SYNC_METHOD -> ""
                SCROLL_WARNING_DISMISSED -> !context.isRunningOnQuest()
            }
    }

    fun <T> get(key: Key, preferences: PlatformPreferences = prefs, default: T? = null): T {
        val default_value: T = default ?: (key.getDefaultValue(context) as T)
        return when (default_value) {
            is Boolean -> preferences.getBoolean(key.name, default_value as Boolean)
            is Float -> preferences.getFloat(key.name, default_value as Float)
            is Int -> preferences.getInt(key.name, default_value as Int)
            is Long -> preferences.getLong(key.name, default_value as Long)
            is String -> preferences.getString(key.name, default_value as String)
            is Set<*> -> preferences.getStringSet(key.name, default_value as Set<String>)
            else -> throw NotImplementedError("$key $default_value ${default_value!!::class.simpleName}")
        } as T
    }

    fun <T> set(key: Key, value: T?, preferences: PlatformPreferences = prefs) {
        preferences.edit {
            @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
            when (value) {
                null -> remove(key.name)
                is Boolean -> putBoolean(key.name, value)
                is Float -> putFloat(key.name, value)
                is Int -> putInt(key.name, value)
                is Long -> putLong(key.name, value)
                is String -> putString(key.name, value)
                is Set<*> -> putStringSet(key.name, value as Set<String>)
                is Enum<*> -> putInt(key.name, value.ordinal)
                else -> throw NotImplementedError("$key ${value!!::class.simpleName}")
            }
        }
    }
}

@Composable
fun <T> Settings.PrefsField<T>.observeSettingsKey(): MutableState<T> {
    val state: MutableState<T> = remember { mutableStateOf(get()) }
    var set_to: T by remember { mutableStateOf(state.value) }

    LaunchedEffect(state.value) {
        if (state.value != set_to) {
            set_to = state.value
            set(set_to)
        }
    }

    OnChangedEffect(this) {
        state.value = get()
    }

    DisposableEffect(this) {
        val listener = prefs.addListener(object : PlatformPreferencesListener {
            override fun onChanged(prefs: PlatformPreferences, key: String) {
                if (key == this@observeSettingsKey.key.name) {
                    set_to = get()
                    state.value = set_to
                }
            }
        })

        onDispose {
            prefs.removeListener(listener)
        }
    }

    return state
}