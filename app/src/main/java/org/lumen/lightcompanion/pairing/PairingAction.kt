package org.lumen.lightcompanion.pairing

sealed interface PairingAction {
    data object OpenManualEntry : PairingAction
    data object OpenQrPlaceholder : PairingAction
    data class ManualCodeChanged(val value: String) : PairingAction
    data object SubmitManualPlaceholder : PairingAction
    data object SubmitQrPlaceholder : PairingAction
    data object CancelPairing : PairingAction
    data object FakeHostApproved : PairingAction
    data object FakeHostRejected : PairingAction
    data object ConnectRequested : PairingAction
    data object FakeTransportConnected : PairingAction
    data class FakeTransportDisconnected(val reason: DisconnectReason) : PairingAction
    data object UnpairRequested : PairingAction
}
