import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.toastbits.sinksabre.SinkSabre
import dev.toastbits.sinksabre.platform.AppContext

fun main() = application {
    val context: AppContext = AppContext()

    Window(onCloseRequest = ::exitApplication) {
        SinkSabre(context)
    }
}
