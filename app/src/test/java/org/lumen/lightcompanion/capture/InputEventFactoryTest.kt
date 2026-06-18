package org.lumen.lightcompanion.capture

import org.junit.Assert.assertEquals
import org.junit.Test
import org.lumen.lightcompanion.domain.event.EventType
import org.lumen.lightcompanion.domain.event.NormalizedCoordinate

class InputEventFactoryTest {
    @Test
    fun touchMoveCreatesSyntheticCompanionEvent() {
        val event = InputEventFactory.touchMove(1, "pointer_1", NormalizedCoordinate(0.4f, 0.6f))

        assertEquals(EventType.TouchMove, event.eventType)
        assertEquals("session_synthetic_demo", event.sessionId)
    }
}
