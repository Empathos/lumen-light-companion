package org.lumen.lightcompanion.transport

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.lumen.lightcompanion.domain.event.CompanionEvent

interface CompanionTransport {
    val connectionState: StateFlow<TransportConnectionState>
    val incomingEvents: Flow<CompanionEvent>

    suspend fun connect(context: TransportSessionContext): Result<Unit>
    suspend fun send(event: CompanionEvent): Result<Unit>
    suspend fun disconnect(reason: TransportDisconnectReason): Result<Unit>
}

data class TransportSessionContext(
    val sessionId: String,
    val surfaceId: String,
    val displayLabel: String,
)

sealed interface TransportConnectionState {
    data object Idle : TransportConnectionState
    data class Connecting(val context: TransportSessionContext) : TransportConnectionState
    data class Connected(val context: TransportSessionContext) : TransportConnectionState
    data class Disconnected(
        val previousContext: TransportSessionContext?,
        val reason: TransportDisconnectReason,
    ) : TransportConnectionState
    data class Failed(val message: String) : TransportConnectionState
}

enum class TransportDisconnectReason { UserRequested, PairingLost, HostUnavailable, LocalDemoEnded }
