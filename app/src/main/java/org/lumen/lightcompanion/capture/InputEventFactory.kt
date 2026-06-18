package org.lumen.lightcompanion.capture

import org.lumen.lightcompanion.domain.event.CompanionEvent
import org.lumen.lightcompanion.domain.event.DeviceRole
import org.lumen.lightcompanion.domain.event.EventType
import org.lumen.lightcompanion.domain.event.NormalizedCoordinate

object InputEventFactory {
    fun touchMove(
        sequence: Long,
        pointerId: String,
        position: NormalizedCoordinate,
    ) = CompanionEvent(
        eventId = "event_synthetic_touch_move_$sequence",
        eventType = EventType.TouchMove,
        sessionId = "session_synthetic_demo",
        surfaceId = "surface_synthetic_hosted_whiteboard",
        deviceRole = DeviceRole.Companion,
        timestamp = "2026-01-01T00:00:00Z",
        sequence = sequence,
        payload = mapOf(
            "viewport_id" to "viewport_phone_touch_surface",
            "pointer_id" to pointerId,
            "x_normalized" to position.x.toString(),
            "y_normalized" to position.y.toString(),
        ),
    )
}
