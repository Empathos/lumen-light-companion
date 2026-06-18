package org.lumen.lightcompanion.domain.event

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CompanionEvent(
    @SerialName("event_id") val eventId: String,
    @SerialName("event_type") val eventType: EventType,
    @SerialName("session_id") val sessionId: String,
    @SerialName("surface_id") val surfaceId: String,
    @SerialName("device_role") val deviceRole: DeviceRole,
    val timestamp: String,
    val sequence: Long,
    val payload: Map<String, String> = emptyMap(),
)

@Serializable
enum class DeviceRole {
    @SerialName("companion") Companion,
    @SerialName("host") Host,
}

@Serializable
enum class EventType {
    @SerialName("touch.select") TouchSelect,
    @SerialName("touch.move") TouchMove,
    @SerialName("touch.resize") TouchResize,
    @SerialName("touch.pan") TouchPan,
    @SerialName("touch.zoom") TouchZoom,
    @SerialName("ink.stroke") InkStroke,
    @SerialName("audio.chunk") AudioChunk,
    @SerialName("gesture.air_draw") GestureAirDraw,
    @SerialName("gesture.approve") GestureApprove,
    @SerialName("gesture.reject") GestureReject,
    @SerialName("feedback.haptic") FeedbackHaptic,
    @SerialName("surface.selection_changed") SurfaceSelectionChanged,
    @SerialName("surface.staged_change_ready") SurfaceStagedChangeReady,
    @SerialName("surface.change_accepted") SurfaceChangeAccepted,
    @SerialName("surface.change_rejected") SurfaceChangeRejected,
    @SerialName("session.recording_started") SessionRecordingStarted,
    @SerialName("session.recording_stopped") SessionRecordingStopped,
    @SerialName("session.connection_changed") SessionConnectionChanged,
}
