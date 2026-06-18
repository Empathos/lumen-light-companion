package org.lumen.lightcompanion.pairing

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PairingReducerTest {
    @Test
    fun fakeDisconnectForcesCaptureInactive() {
        val connected = PairingState.PairedConnected("session_synthetic_demo", "surface_synthetic_hosted_whiteboard")
        val next = reducePairing(connected, PairingAction.FakeTransportDisconnected(DisconnectReason.HostUnavailable))

        assertTrue(next is PairingState.PairedDisconnected)
        assertFalse(next is PairingState.PairedConnected)
    }
}
