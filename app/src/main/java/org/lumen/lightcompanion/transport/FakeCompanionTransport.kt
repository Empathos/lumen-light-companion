package org.lumen.lightcompanion.transport

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.lumen.lightcompanion.domain.event.CompanionEvent

class FakeCompanionTransport : CompanionTransport {
    private val _connectionState = MutableStateFlow<TransportConnectionState>(TransportConnectionState.Idle)
    private val _incomingEvents = MutableSharedFlow<CompanionEvent>(extraBufferCapacity = 16)
    private val _sentEvents = mutableListOf<CompanionEvent>()

    override val connectionState: StateFlow<TransportConnectionState> = _connectionState
    override val incomingEvents: SharedFlow<CompanionEvent> = _incomingEvents
    val sentEvents: List<CompanionEvent> get() = _sentEvents.toList()

    override suspend fun connect(context: TransportSessionContext): Result<Unit> {
        _connectionState.value = TransportConnectionState.Connected(context)
        return Result.success(Unit)
    }

    override suspend fun send(event: CompanionEvent): Result<Unit> {
        if (_connectionState.value !is TransportConnectionState.Connected) {
            return Result.failure(IllegalStateException("Fake transport is disconnected."))
        }
        _sentEvents += event
        return Result.success(Unit)
    }

    override suspend fun disconnect(reason: TransportDisconnectReason): Result<Unit> {
        val previous = (_connectionState.value as? TransportConnectionState.Connected)?.context
        _connectionState.value = TransportConnectionState.Disconnected(previous, reason)
        return Result.success(Unit)
    }

    fun enqueueHostEvent(event: CompanionEvent) {
        _incomingEvents.tryEmit(event)
    }
}
