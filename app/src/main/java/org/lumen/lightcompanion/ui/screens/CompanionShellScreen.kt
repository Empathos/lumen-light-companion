package org.lumen.lightcompanion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.lumen.lightcompanion.ui.components.StatusPill
import org.lumen.lightcompanion.ui.state.CompanionShellState

@Composable
fun CompanionShellScreen(state: CompanionShellState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Lumen Companion", style = MaterialTheme.typography.headlineSmall)
            Text("Hosted session input", style = MaterialTheme.typography.bodyMedium)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusPill("Pairing", state.pairingStatus, state.isPaired)
            StatusPill("Connection", state.connectionStatus, state.isConnected)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusPill("Touch", state.touchStatus, state.canCaptureTouch)
            StatusPill("Ink", state.inkStatus, state.canCaptureInk)
            StatusPill("Audio", state.audioStatus, state.canRequestAudio)
            StatusPill("Haptics", state.hapticsStatus, state.hapticsEnabled)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color(0xFFEAF0F6)),
            contentAlignment = Alignment.Center,
        ) {
            Text("Normalized companion input surface")
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(enabled = state.primaryActionEnabled, onClick = {}) {
                Text(state.primaryActionLabel)
            }
            OutlinedButton(enabled = state.secondaryActionEnabled, onClick = {}) {
                Text(state.secondaryActionLabel)
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Host feedback", style = MaterialTheme.typography.titleMedium)
            Text(state.latestFeedback)
            Text("Canonical surface state remains on the hosted Lumen session.")
        }
    }
}
