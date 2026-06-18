package org.lumen.lightcompanion.feedback

import org.lumen.lightcompanion.domain.event.CompanionEvent
import org.lumen.lightcompanion.domain.event.DeviceRole
import org.lumen.lightcompanion.domain.event.EventType

fun mapFeedbackToHaptic(
    event: CompanionEvent,
    mode: HapticsMode = HapticsMode.SystemDefault,
    connected: Boolean = true,
): HapticPattern {
    if (!connected || mode == HapticsMode.Silent || event.deviceRole != DeviceRole.Host) return HapticPattern.None

    val pattern = when (event.eventType) {
        EventType.FeedbackHaptic -> HapticPattern.fromWireName(event.payload["pattern"])
        EventType.SurfaceSelectionChanged -> HapticPattern.SelectionChangedTick
        EventType.SurfaceStagedChangeReady -> HapticPattern.StagedChangeReadyBuzz
        EventType.SurfaceChangeAccepted -> HapticPattern.ChangeAcceptedFirmTick
        EventType.SurfaceChangeRejected -> HapticPattern.ChangeRejectedSoftReject
        EventType.SessionRecordingStarted -> HapticPattern.RecordingStartedPulse
        EventType.SessionRecordingStopped -> HapticPattern.RecordingStoppedSettle
        EventType.SessionConnectionChanged -> when (event.payload["status"]) {
            "connected" -> HapticPattern.ConnectionRestoredTick
            "disconnected", "lost" -> HapticPattern.PairingLostFade
            else -> HapticPattern.None
        }
        else -> HapticPattern.None
    }

    return if (mode == HapticsMode.Reduced && pattern in reducedSuppressedPatterns) {
        HapticPattern.None
    } else pattern
}

private val reducedSuppressedPatterns = setOf(
    HapticPattern.SelectionChangedTick,
    HapticPattern.SelectionMoveTick,
    HapticPattern.SnapAlignmentDoubleTick,
)
