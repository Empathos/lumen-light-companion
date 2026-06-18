package org.lumen.lightcompanion.pairing

sealed interface PairingState {
    data object Unpaired : PairingState
    data class ManualEntryOpen(val enteredCode: String = "") : PairingState
    data object QrScanPlaceholder : PairingState
    data class PairingRequested(val method: PairingMethod, val displayLabel: String) : PairingState
    data class PairedDisconnected(
        val sessionLabel: String,
        val surfaceLabel: String,
        val reason: DisconnectReason,
    ) : PairingState
    data class PairedConnecting(val sessionLabel: String, val surfaceLabel: String) : PairingState
    data class PairedConnected(
        val sessionLabel: String,
        val surfaceLabel: String,
        val canCaptureTouch: Boolean = true,
        val canCaptureInk: Boolean = true,
        val canCaptureAudio: Boolean = false,
    ) : PairingState
    data class PairingFailed(val method: PairingMethod, val message: String) : PairingState
}

enum class PairingMethod { ManualCode, QrPlaceholder }
enum class DisconnectReason { NotConnected, UserDisconnected, HostUnavailable, PairingLost }
