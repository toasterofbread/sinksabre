package dev.toastbits.sinksabre.ui.layout

import androidx.compose.runtime.*
import dev.toastbits.sinksabre.platform.AppContext
import dev.toastbits.sinksabre.settings.*
import dev.toastbits.sinksabre.ui.component.MenuTitleBar
import dev.toastbits.sinksabre.ui.component.settingsfield.*
import dev.toastbits.composekit.utils.composable.NullableValueAnimatedVisibility
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import dev.toastbits.composekit.utils.composable.LargeDropdownMenu
import dev.toastbits.sinksabre.sync.SyncMethod
import dev.toastbits.sinksabre.sync.getName

@Composable
fun SettingsMenu(context: AppContext, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        MenuTitleBar {
            Text("Settings")
        }

        BooleanSettingsField(context.settings.SYNC_ON_START)

        SyncMethodField(context)
    }
}

@Composable
private fun SyncMethodField(context: AppContext) {
    var show_sync_method_selector: Boolean by remember { mutableStateOf(false) }
    var sync_method: SyncMethod? by SyncMethod.observe(context)

    LargeDropdownMenu(
        show_sync_method_selector,
        { show_sync_method_selector = false },
        SyncMethod.Type.entries.size + 1,
        sync_method?.getType()?.ordinal?.plus(1) ?: 0,
        {
            val type: SyncMethod.Type? =
                if (it == 0) null
                else SyncMethod.Type.entries[it - 1]

            Text(type.getName())
        }
    ) {
        sync_method =
            if (it == 0) null
            else SyncMethod.Type.entries[it - 1].create()
        show_sync_method_selector = false
    }

    SettingsField(
        name = context.settings.SYNC_METHOD.getName(),
        description = context.settings.SYNC_METHOD.getDescription(),
        oneline = true
    ) {
        Button({ show_sync_method_selector = true }) {
            Text(sync_method?.getType().getName())
        }
    }

    NullableValueAnimatedVisibility(sync_method) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            it?.ConfigurationItems(context) {
                sync_method = it
            }
        }
    }
}
