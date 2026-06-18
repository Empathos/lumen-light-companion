package org.lumen.lightcompanion.feedback

enum class HapticPattern(val wireName: String) {
    SelectionChangedTick("selection_changed_tick"),
    SelectionMoveTick("selection_move_tick"),
    SnapAlignmentDoubleTick("snap_alignment_double_tick"),
    StagedChangeReadyBuzz("staged_change_ready_buzz"),
    ChangeAcceptedFirmTick("change_accepted_firm_tick"),
    ChangeRejectedSoftReject("change_rejected_soft_reject"),
    BoundaryCollisionReject("boundary_collision_reject"),
    RecordingStartedPulse("recording_started_pulse"),
    RecordingStoppedSettle("recording_stopped_settle"),
    PairingLostFade("pairing_lost_fade"),
    ConnectionRestoredTick("connection_restored_tick"),
    None("none");

    companion object {
        fun fromWireName(value: String?) = entries.firstOrNull { it.wireName == value } ?: None
    }
}

enum class HapticsMode { SystemDefault, Enabled, Reduced, Silent }

interface HapticFeedbackInvoker {
    fun perform(pattern: HapticPattern)
}

class FakeHapticFeedbackInvoker : HapticFeedbackInvoker {
    private val _performed = mutableListOf<HapticPattern>()
    val performed: List<HapticPattern> get() = _performed.toList()

    override fun perform(pattern: HapticPattern) {
        if (pattern != HapticPattern.None) _performed += pattern
    }
}
