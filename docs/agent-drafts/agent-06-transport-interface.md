# Agent 6 Draft Packet: Transport Interface

## Scope

This packet proposes a transport-independent boundary for the Android scaffold.
It gives app, pairing, event, and UI code a small interface for sending
companion input events and receiving host feedback/status events without binding
the scaffold to WebSocket, HTTP, WebRTC, host URLs, signaling, credentials, or
background reconnect behavior.

The first implementation should be fake/local and deterministic. It should
support local demos and unit tests only. The hosted Lumen surface remains the
owner of canonical session state, object state, transcript state, and feedback.

## Transport-Independent Interface

The app should depend on a narrow interface:

```kotlin
interface CompanionTransport {
    val connectionState: StateFlow<TransportConnectionState>
    val incomingEvents: Flow<CompanionEvent<out EventPayload>>

    suspend fun connect(context: TransportSessionContext): Result<Unit>
    suspend fun send(event: CompanionEvent<out CompanionInputPayload>): Result<Unit>
    suspend fun disconnect(reason: TransportDisconnectReason): Result<Unit>
}
```

Recommended supporting types:

```kotlin
data class TransportSessionContext(
    val sessionId: SessionId,
    val surfaceId: SurfaceId,
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

enum class TransportDisconnectReason {
    UserRequested,
    PairingLost,
    HostUnavailable,
    LocalDemoEnded,
}
```

Rules:

- `CompanionTransport` must accept and emit domain events, not wire-format JSON
  strings.
- The interface must not expose URLs, sockets, clients, signaling details, or
  retry configuration.
- `send` should fail predictably when disconnected.
- Disconnect must be observable so pairing, capture, audio, and UI state can
  move to safe inactive states.
- Incoming host events should use the same event envelope proposed by Agent 3.

## Fake/Local Implementation

Add a deterministic fake transport for previews, local demo state, and unit
tests:

```kotlin
class FakeCompanionTransport(
    private val clock: Clock,
) : CompanionTransport
```

Expected behavior:

- Starts in `Idle`.
- `connect` moves `Idle` or `Disconnected` to `Connecting`, then to
  `Connected` through an explicit fake action or test helper.
- `send` records accepted companion events in memory when connected.
- `send` rejects events when idle, connecting, disconnected, or failed.
- Test helpers may enqueue synthetic host feedback events such as
  `feedback.haptic`, `surface.selection_changed`, and
  `session.connection_changed`.
- `disconnect` moves to `Disconnected`, clears any active demo-only flow, and
  emits a synthetic `session.connection_changed` event if useful for UI tests.

The fake should not:

- Open sockets.
- Read environment variables.
- Contain hostnames, IP addresses, ports, pairing secrets, device IDs, logs, or
  deployment configuration.
- Buffer media or input for later upload while disconnected.
- Attempt automatic reconnect loops.

## Future Transport Boundaries

Future real transports should adapt to `CompanionTransport` without changing UI,
pairing, touch, ink, audio, haptic, or gesture code.

WebSocket boundary:

- Candidate for low-latency bidirectional control and feedback events.
- Owns socket lifecycle, heartbeat, wire JSON encode/decode, and backpressure.
- Must not leak concrete URLs into domain or UI code.
- Should be introduced only after the host event protocol stabilizes.

HTTP boundary:

- Candidate for pairing requests, session metadata, and low-frequency status.
- Should remain request/response and not pretend to be the realtime event lane.
- Must keep authentication and deployment values in private downstream config.

WebRTC boundary:

- Candidate for realtime audio and possible camera streams.
- Should be isolated from control-event transport because media consent,
  permissions, retention, and signaling have different risk profiles.
- Signaling format and media routes are explicitly out of scope for this public
  scaffold.

The public scaffold may define interface names for these adapters later, but it
should not add live clients or placeholder endpoints in this packet.

## Proposed Files

```text
app/src/main/java/org/lumen/lightcompanion/transport/CompanionTransport.kt
app/src/main/java/org/lumen/lightcompanion/transport/TransportConnectionState.kt
app/src/main/java/org/lumen/lightcompanion/transport/TransportSessionContext.kt
app/src/main/java/org/lumen/lightcompanion/transport/TransportDisconnectReason.kt
app/src/main/java/org/lumen/lightcompanion/transport/FakeCompanionTransport.kt
app/src/test/java/org/lumen/lightcompanion/transport/FakeCompanionTransportTest.kt
```

If the integrator chooses a different package or event model path, keep these
file roles and align imports with the accepted Agent 1 and Agent 3 outputs.

## Acceptance Criteria

- App code depends on `CompanionTransport`, not a concrete network client.
- Fake transport supports deterministic state-transition tests.
- Fake transport records sent companion events only while connected.
- Fake transport can emit synthetic host feedback/status events.
- Disconnection is observable and forces callers to stop capture through pairing
  and UI state.
- No real WebSocket URLs, HTTP endpoints, WebRTC signaling, private hosts,
  credentials, device identifiers, live logs, transcripts, screenshots, media,
  or deployment config are added.
- Transport code keeps companion events transport-independent; serialization
  stays in Agent 4's boundary.

## Test Cases

- Initial state is `Idle`.
- `connect` with `session_synthetic_demo` and
  `surface_synthetic_hosted_whiteboard` reaches `Connected` through deterministic
  fake control.
- `send` succeeds while connected and records the event in order.
- `send` fails before connect and after disconnect.
- Enqueued synthetic host feedback is emitted through `incomingEvents`.
- `disconnect(UserRequested)` moves to `Disconnected` and prevents additional
  sends.
- A fake host-lost event moves to `Disconnected(HostUnavailable)` and emits a
  connection-changed status event if that projection is implemented.

## Verification Commands

```bash
./gradlew :app:testDebugUnitTest
python3 scripts/validate_companion_event.py examples/companion-event.example.json
git diff --check
rg -n "wss?://|https?://|[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+|password|secret|token|credential|device_[A-Za-z0-9_-]+|transcript|screenshot" app/src docs/agent-drafts/agent-06-transport-interface.md || true
```

If Gradle files do not exist yet, record the blocked Android test command with
the exact missing tool or file and still run the Python schema validation and
`git diff --check`.

## Dependencies

- Agent 1 Android/Kotlin scaffold.
- Agent 3 event envelope and payload model.
- Agent 4 serialization boundary for future wire adapters.
- Kotlin coroutines `Flow`/`StateFlow` for observable connection and event
  streams.
- JUnit or the test dependency chosen by the Android scaffold.

No networking, WebRTC, QR, media, or dependency-injection library is required for
this packet.

## Risks And Open Questions

- The final Agent 3 event model may use strongly typed payloads, generic JSON
  payloads, or a transitional hybrid. The transport interface should follow that
  decision rather than introduce its own event shape.
- `Result<Unit>` is simple for the scaffold, but later production code may need
  richer send failure types for user-facing retry or diagnostics.
- Flow buffer behavior should be explicit once real transports exist; the fake
  can keep small deterministic in-memory queues for tests.
- Audio and camera transport need separate consent and lifecycle decisions before
  any real WebRTC or media upload work.
- Pairing may later produce a transport context that includes authenticated
  routing metadata. That metadata belongs behind adapter boundaries and private
  downstream configuration, not in public examples.
