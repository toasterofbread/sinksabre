package dev.toastbits.sinksabre.ui.layout

import androidx.compose.runtime.*
import dev.toastbits.sinksabre.platform.AppContext
import dev.toastbits.sinksabre.ui.component.MenuTitleBar
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text

@Composable
fun LandingMenu(context: AppContext, modifier: Modifier = Modifier) {
    Column(modifier) {
        MenuTitleBar {
            Text("Awaiting command")
        }
    }
}
