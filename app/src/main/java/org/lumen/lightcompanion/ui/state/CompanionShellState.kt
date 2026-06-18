package org.lumen.lightcompanion.ui.state

data class CompanionShellState(
    val isPaired: Boolean,
    val isConnected: Boolean,
    val pairingStatus: String,
    val connectionStatus: String,
    val touchStatus: String,
    val inkStatus: String,
    val audioStatus: String,
    val hapticsStatus: String,
    val canCaptureTouch: Boolean,
    val canCaptureInk: Boolean,
    val canRequestAudio: Boolean,
    val hapticsEnabled: Boolean,
    val primaryActionLabel: String,
    val secondaryActionLabel: String,
    val primaryActionEnabled: Boolean,
    val secondaryActionEnabled: Boolean,
    val latestFeedback: String,
) {
    companion object {
        fun demoConnected() = CompanionShellState(
            isPaired = true,
            isConnected = true,
            pairingStatus = "Paired",
            connectionStatus = "Fake local",
            touchStatus = "Ready",
            inkStatus = "Ready",
            audioStatus = "Inactive",
            hapticsStatus = "Enabled",
            canCaptureTouch = true,
            canCaptureInk = true,
            canRequestAudio = false,
            hapticsEnabled = true,
            primaryActionLabel = "Disconnect",
            secondaryActionLabel = "Audio placeholder",
            primaryActionEnabled = true,
            secondaryActionEnabled = false,
            latestFeedback = "No host feedback yet",
        )
    }
}
