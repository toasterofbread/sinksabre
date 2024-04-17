package dev.toastbits.sinksabre

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import dev.toastbits.sinksabre.platform.AppContext
import dev.toastbits.sinksabre.SinkSabre
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val coroutine_scope: CoroutineScope = CoroutineScope(Job())
        val context: AppContext = AppContext(this, coroutine_scope)

        setContent {
            SinkSabre(context)
        }
    }
}
