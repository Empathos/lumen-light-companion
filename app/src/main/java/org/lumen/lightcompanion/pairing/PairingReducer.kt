package org.lumen.lightcompanion.pairing

private const val DemoSession = "session_synthetic_demo"
private const val DemoSurface = "surface_synthetic_hosted_whiteboard"

fun reducePairing(state: PairingState, action: PairingAction): PairingState = when (action) {
    PairingAction.OpenManualEntry -> if (state is PairingState.Unpaired || state is PairingState.PairingFailed) {
        PairingState.ManualEntryOpen()
    } else state
    PairingAction.OpenQrPlaceholder -> if (state is PairingState.Unpaired || state is PairingState.PairingFailed) {
        PairingState.QrScanPlaceholder
    } else state
    is PairingAction.ManualCodeChanged -> if (state is PairingState.ManualEntryOpen) {
        state.copy(enteredCode = action.value)
    } else state
    PairingAction.SubmitManualPlaceholder -> if (state is PairingState.ManualEntryOpen) {
        PairingState.PairingRequested(PairingMethod.ManualCode, "manual_synthetic_pairing")
    } else state
    PairingAction.SubmitQrPlaceholder -> if (state is PairingState.QrScanPlaceholder) {
        PairingState.PairingRequested(PairingMethod.QrPlaceholder, "qr_synthetic_pairing_placeholder")
    } else state
    PairingAction.CancelPairing, PairingAction.UnpairRequested -> PairingState.Unpaired
    PairingAction.FakeHostApproved -> if (state is PairingState.PairingRequested) {
        PairingState.PairedDisconnected(DemoSession, DemoSurface, DisconnectReason.NotConnected)
    } else state
    PairingAction.FakeHostRejected -> if (state is PairingState.PairingRequested) {
        PairingState.PairingFailed(state.method, "Synthetic pairing was rejected")
    } else state
    PairingAction.ConnectRequested -> if (state is PairingState.PairedDisconnected) {
        PairingState.PairedConnecting(state.sessionLabel, state.surfaceLabel)
    } else state
    PairingAction.FakeTransportConnected -> if (state is PairingState.PairedConnecting) {
        PairingState.PairedConnected(state.sessionLabel, state.surfaceLabel)
    } else state
    is PairingAction.FakeTransportDisconnected -> when (state) {
        is PairingState.PairedConnected -> PairingState.PairedDisconnected(state.sessionLabel, state.surfaceLabel, action.reason)
        is PairingState.PairedConnecting -> PairingState.PairedDisconnected(state.sessionLabel, state.surfaceLabel, action.reason)
        is PairingState.PairedDisconnected -> state.copy(reason = action.reason)
        else -> state
    }
}
