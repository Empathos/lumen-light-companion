# Agent 3 Draft Packet: Companion Event Models

## Scope

Define Kotlin domain models for companion input events and host feedback events
that align with the public schema and product boundary. These models should give
later serialization, transport, touch, ink, audio, gesture, and haptic packets a
small shared contract without making the Android companion a canonical
whiteboard.

The companion may model local capture state and event payloads. The hosted Lumen
surface remains responsible for canonical session state, object state, surface
coordinates, transcript state, staged changes, and durable artifacts.

## Proposed Files

```text
app/src/main/java/org/lumen/lightcompanion/domain/event/CompanionEvent.kt
app/src/main/java/org/lumen/lightcompanion/domain/event/EventType.kt
app/src/main/java/org/lumen/lightcompanion/domain/event/EventIds.kt
app/src/main/java/org/lumen/lightcompanion/domain/event/NormalizedCoordinate.kt
app/src/main/java/org/lumen/lightcompanion/domain/event/TouchPayload.kt
app/src/main/java/org/lumen/lightcompanion/domain/event/InkPayload.kt
app/src/main/java/org/lumen/lightcompanion/domain/event/AudioPayload.kt
app/src/main/java/org/lumen/lightcompanion/domain/event/GesturePayload.kt
app/src/main/java/org/lumen/lightcompanion/domain/event/FeedbackPayload.kt
app/src/test/java/org/lumen/lightcompanion/domain/event/CompanionEventModelTest.kt
```

Integrator note: Agent 4 owns JSON serialization and fixture round-trip tests.
The tests in this packet can stay constructor/value validation tests if
serialization has not landed yet.

## Kotlin Domain Model Proposal

### Event Envelope

Use one transport-independent envelope that mirrors
`schemas/companion-event.schema.json`:

```kotlin
data class CompanionEvent<P : EventPayload>(
    val eventId: EventId,
    val eventType: EventType,
    val sessionId: SessionId,
    val surfaceId: SurfaceId,
    val deviceRole: DeviceRole,
    val timestamp: Instant,
    val sequence: Long,
    val payload: P,
)
```

Recommended value classes:

```kotlin
@JvmInline value class EventId(val value: String)
@JvmInline value class SessionId(val value: String)
@JvmInline value class SurfaceId(val value: String)
@JvmInline value class ViewportId(val value: String)
@JvmInline value class ObjectId(val value: String)
@JvmInline value class PointerId(val value: String)
@JvmInline value class StrokeId(val value: String)
```

Keep IDs opaque. Do not parse host object semantics from them.

### Event Types

Represent schema event names as constants or enum values with stable wire names:

```kotlin
enum class EventType(val wireName: String) {
    TouchSelect("touch.select"),
    TouchMove("touch.move"),
    TouchResize("touch.resize"),
    TouchPan("touch.pan"),
    TouchZoom("touch.zoom"),
    InkStroke("ink.stroke"),
    AudioChunk("audio.chunk"),
    GestureAirDraw("gesture.air_draw"),
    GestureApprove("gesture.approve"),
    GestureReject("gesture.reject"),
    FeedbackHaptic("feedback.haptic"),
    SurfaceSelectionChanged("surface.selection_changed"),
    SurfaceStagedChangeReady("surface.staged_change_ready"),
    SurfaceChangeAccepted("surface.change_accepted"),
    SurfaceChangeRejected("surface.change_rejected"),
    SessionRecordingStarted("session.recording_started"),
    SessionRecordingStopped("session.recording_stopped"),
    SessionConnectionChanged("session.connection_changed"),
}
```

`DeviceRole` should be limited to the schema roles:

```kotlin
enum class DeviceRole { Companion, Host }
```

### Normalized Coordinates

Use normalized coordinates as explicit value objects so later input code cannot
silently treat local pixels as host coordinates:

```kotlin
data class NormalizedCoordinate(
    val x: Float,
    val y: Float,
)

data class NormalizedDelta(
    val dx: Float,
    val dy: Float,
)
```

Constructor validation should require finite values. Coordinate values should be
within `0.0..1.0` for positions. Deltas may be negative and can exceed that range
during gestures, but must remain finite. The host resolves normalized values into
canonical surface coordinates.

### Payload Interfaces

Use sealed payload families. Payloads represent observed input or received
feedback, not canonical whiteboard entities:

```kotlin
sealed interface EventPayload
sealed interface CompanionInputPayload : EventPayload
sealed interface HostFeedbackPayload : EventPayload
```

### Touch Payloads

Touch payloads cover select, move, resize, pan, and zoom at scaffold level:

```kotlin
data class TouchPayload(
    val viewportId: ViewportId,
    val pointerId: PointerId,
    val position: NormalizedCoordinate,
    val targetObjectId: ObjectId? = null,
    val delta: NormalizedDelta? = null,
    val scaleDelta: Float? = null,
    val pressure: Float? = null,
) : CompanionInputPayload
```

`targetObjectId` is optional because the host resolves object targeting. The
companion should not maintain a local object graph.

### Ink Payloads

Ink payloads should carry a stable stroke identifier and normalized points:

```kotlin
data class InkStrokePayload(
    val viewportId: ViewportId,
    val strokeId: StrokeId,
    val points: List<InkPoint>,
    val phase: InkStrokePhase,
) : CompanionInputPayload

data class InkPoint(
    val position: NormalizedCoordinate,
    val pressure: Float? = null,
    val elapsedMillis: Long? = null,
)

enum class InkStrokePhase { Started, Continued, Ended, Cancelled }
```

Do not model rendered stroke objects, layers, or durable ink state on the
companion. The host renders and stores accepted stroke output.

### Audio State Payloads

The schema currently names `audio.chunk`, but this scaffold should avoid raw
media modeling beyond explicit state and bounded metadata:

```kotlin
data class AudioChunkPayload(
    val captureState: AudioCaptureState,
    val chunkId: String? = null,
    val durationMillis: Long? = null,
    val format: AudioFormatHint? = null,
) : CompanionInputPayload

enum class AudioCaptureState { Starting, Active, Pausing, Stopped }

data class AudioFormatHint(
    val codec: String,
    val sampleRateHz: Int? = null,
)
```

This packet should not add byte arrays, files, transcript text, or retention
metadata. Agent 8 owns permission and inactive-on-disconnect behavior.

### Gesture Command Payloads

Gesture payloads are derived commands, not raw camera frames:

```kotlin
sealed interface GesturePayload : CompanionInputPayload

data class AirDrawGesturePayload(
    val viewportId: ViewportId,
    val gestureId: String,
    val points: List<NormalizedCoordinate>,
) : GesturePayload

data object ApproveGesturePayload : GesturePayload
data object RejectGesturePayload : GesturePayload
```

Future gestures can extend this family without changing host ownership.

### Feedback Payloads

Host-to-companion feedback models should be deliberately small and UI/haptic
oriented:

```kotlin
sealed interface FeedbackPayload : HostFeedbackPayload

data class HapticFeedbackPayload(
    val pattern: HapticPattern,
    val intensity: Float? = null,
) : FeedbackPayload

enum class HapticPattern {
    SelectionChanged,
    SelectionMoveTick,
    SnapAlignment,
    StagedChangeReady,
    ChangeAccepted,
    ChangeRejected,
    RecordingStarted,
    RecordingStopped,
    Disconnected,
}

data class SurfaceStatusPayload(
    val selectedObjectId: ObjectId? = null,
    val stagedChangeId: String? = null,
) : FeedbackPayload

data class SessionStatusPayload(
    val connectionState: ConnectionState? = null,
    val recordingState: RecordingState? = null,
) : FeedbackPayload

enum class ConnectionState { Connected, Reconnecting, Disconnected }
enum class RecordingState { Recording, Stopped }
```

`SurfaceStatusPayload` may expose opaque identifiers and status hints only. It
must not contain canonical surface snapshots or object models.

## Schema Alignment Notes

- `CompanionEvent` maps directly to required schema fields:
  `event_id`, `event_type`, `session_id`, `surface_id`, `device_role`,
  `timestamp`, `sequence`, and `payload`.
- `EventType.wireName` should exactly match the schema enum.
- `DeviceRole` should map only to `companion` and `host`.
- `TouchPayload` covers the existing synthetic example fields:
  `viewport_id`, `object_id`, `pointer_id`, `x_normalized`, `y_normalized`,
  `dx_normalized`, `dy_normalized`, and `pressure`.
- `expected_host_response` from the public example is a validation/demo concept,
  not part of the Android runtime event envelope.
- Serialization annotations and wire-name mapping belong to Agent 4 unless the
  integrator decides to land model and serialization together.

## Acceptance Criteria

- Models cover scaffold-level touch, ink, audio state, gesture command, haptic
  feedback, surface status, and session status events.
- Event type names align exactly with `schemas/companion-event.schema.json` and
  `docs/PRD.md`.
- Normalized coordinate types make the host coordinate boundary explicit.
- Companion input payloads do not define canonical host surface state, rendered
  objects, transcript state, artifacts, or memory output.
- Audio and gesture models avoid raw media content and require later explicit
  permission/capture handling.
- Public-safe constraints are preserved: no real hostnames, device identifiers,
  credentials, session records, transcripts, screenshots, logs, or deployment
  configuration.

## Verification Commands

After integration, run the checks available for the current scaffold:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
python3 scripts/validate_companion_event.py examples/companion-event.example.json
rg -n "http://|https://|[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+|password|secret|token|credential|device_[A-Za-z0-9_-]+|transcript|screenshot|live session" .
```

If the Android scaffold has not landed yet, at minimum run:

```bash
python3 scripts/validate_companion_event.py examples/companion-event.example.json
git diff --check
```

Expected schema validation output remains:

```text
LUMEN_LIGHT_COMPANION_EVENT_OK
```

## Dependencies

- Kotlin standard library.
- `java.time.Instant` if `minSdk` and desugaring choices support it; otherwise
  use an agreed timestamp wrapper and let serialization handle ISO-8601 strings.
- No serialization dependency is required in this packet. Agent 4 should choose
  and configure serialization, likely `kotlinx.serialization`, if it matches the
  Android scaffold.
- Unit tests can use JUnit from the build scaffold.

## Risks And Open Questions

- Kotlin `data object` requires modern Kotlin; use ordinary singleton objects if
  the integrator chooses an older Kotlin version.
- The current JSON schema leaves `payload` open-ended. These Kotlin models are a
  stronger scaffold contract and may reveal schema fields that should be
  formalized later.
- `audio.chunk` is named like media transfer, but this packet intentionally
  models only explicit state and bounded metadata. Real audio payload transport
  should wait for media and permission packets.
- Haptic pattern names are scaffold-level hints. Agent 8 should map them to
  Android vibration primitives or fake invokers.
- Surface and session feedback payloads should remain status hints. If later
  work needs full host snapshots, that belongs in host protocol design, not the
  companion domain model.
