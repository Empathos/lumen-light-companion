package org.lumen.lightcompanion.capture

enum class CaptureCapability { Touch, Ink, Audio, Gesture }
enum class PermissionGate { NotRequired, Required, Granted, Denied, DisabledByConnection, DisabledByFeatureFlag }
enum class AudioCaptureState { Inactive, PermissionRequired, StartRequested, ActivePlaceholder, StopRequested, Blocked, DisconnectedForcedInactive }

data class CaptureConnectionGate(
    val paired: Boolean,
    val connected: Boolean,
    val stagedChangeReady: Boolean = false,
)

fun canCaptureTouch(gate: CaptureConnectionGate) = gate.paired && gate.connected
fun canCaptureInk(gate: CaptureConnectionGate) = gate.paired && gate.connected
fun canRequestAudio(gate: CaptureConnectionGate) = gate.paired && gate.connected
fun canSendGestureCommand(gate: CaptureConnectionGate) = gate.paired && gate.connected
fun canApproveOrReject(gate: CaptureConnectionGate) = gate.paired && gate.connected && gate.stagedChangeReady
