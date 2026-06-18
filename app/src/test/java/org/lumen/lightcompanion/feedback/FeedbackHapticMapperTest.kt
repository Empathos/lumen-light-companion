package org.lumen.lightcompanion.feedback

import org.junit.Assert.assertEquals
import org.junit.Test
import org.lumen.lightcompanion.domain.event.CompanionEvent
import org.lumen.lightcompanion.domain.event.DeviceRole
import org.lumen.lightcompanion.domain.event.EventType

class FeedbackHapticMapperTest {
    @Test
    fun hostHapticFeedbackMapsToPattern() {
        val event = CompanionEvent(
            eventId = "event_synthetic_feedback_haptic_001",
            eventType = EventType.FeedbackHaptic,
            sessionId = "session_synthetic_demo",
            surfaceId = "surface_synthetic_hosted_whiteboard",
            deviceRole = DeviceRole.Host,
            timestamp = "2026-01-01T00:00:02Z",
            sequence = 19,
            payload = mapOf("pattern" to "selection_move_tick"),
        )

        assertEquals(HapticPattern.SelectionMoveTick, mapFeedbackToHaptic(event))
    }
}
