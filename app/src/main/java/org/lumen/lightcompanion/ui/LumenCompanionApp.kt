package org.lumen.lightcompanion.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import org.lumen.lightcompanion.ui.screens.CompanionShellScreen
import org.lumen.lightcompanion.ui.state.CompanionShellState

@Composable
fun LumenCompanionApp(state: CompanionShellState = CompanionShellState.demoConnected()) {
    Surface {
        CompanionShellScreen(state = state)
    }
}
