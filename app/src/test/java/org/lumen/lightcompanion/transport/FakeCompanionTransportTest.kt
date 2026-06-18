package org.lumen.lightcompanion.transport

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.lumen.lightcompanion.capture.InputEventFactory
import org.lumen.lightcompanion.domain.event.NormalizedCoordinate

class FakeCompanionTransportTest {
    @Test
    fun fakeTransportRejectsSendWhenDisconnected() = runTest {
        val transport = FakeCompanionTransport()
        val event = InputEventFactory.touchMove(1, "pointer_1", NormalizedCoordinate(0.1f, 0.2f))

        assertTrue(transport.send(event).isFailure)

        transport.connect(TransportSessionContext("session_synthetic_demo", "surface_synthetic_hosted_whiteboard", "Demo"))
        assertTrue(transport.send(event).isSuccess)
        assertEquals(1, transport.sentEvents.size)
    }
}
