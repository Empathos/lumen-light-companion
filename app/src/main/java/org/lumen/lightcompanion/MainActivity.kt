package org.lumen.lightcompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.lumen.lightcompanion.ui.LumenCompanionApp
import org.lumen.lightcompanion.ui.theme.LumenCompanionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LumenCompanionTheme {
                LumenCompanionApp()
            }
        }
    }
}
