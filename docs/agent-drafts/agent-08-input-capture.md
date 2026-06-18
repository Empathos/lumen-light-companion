# Agent 8 Draft Packet: Input Capture Scaffolds

## Scope

This packet proposes public-safe Android scaffolds for companion input capture:
touch, ink, audio capture state, gesture command placeholders, permission gates,
and disabled-when-disconnected behavior.

The scaffold emits companion input events only. The hosted Lumen surface remains
the canonical owner of session state, object resolution, surface coordinates,
transcript state, media processing, staged changes, and durable artifacts.

This packet does not implement raw microphone capture, camera capture, live media
streaming, real gesture recognition, real host pairing, or transport-specific
behavior.

## Capture Placeholders

### Touch

- Capture normalized pointer input only when paired and connected.
- Emit schema-aligned placeholder event types:
  - `touch.select`
  - `touch.move`
  - `touch.resize`
  - `touch.pan`
  - `touch.zoom`
- Payload fields should stay close to the existing synthetic example:
  `viewport_id`, optional `object_id`, `pointer_id`, `x_normalized`,
  `y_normalized`, optional normalized deltas, optional `pressure`, and optional
  gesture-specific metadata such as scale delta.
- Object targeting remains advisory. The host resolves object identity and
  canonical coordinates.

### Ink

- Capture stroke intent as normalized points, not rendered whiteboard state.
- Emit `ink.stroke` with a synthetic/client-generated `stroke_id`, `viewport_id`,
  phase, sequence, and normalized points.
- Supported scaffold phases: `started`, `continued`, `ended`, `cancelled`.
- Pending local strokes should be cancelled or dropped on disconnect. Do not
  persist strokes as canonical artifacts on the companion.

### Audio State

- Model explicit user-started audio state without recording bytes.
- Use visible states such as `inactive`, `permission_required`,
  `start_requested`, `active_placeholder`, `stop_requested`, `blocked`, and
  `disconnected_forced_inactive`.
- Do not add byte arrays, audio files, waveform buffers, transcripts, retention
  metadata, background capture, or streaming clients.
- If event models require an `audio.chunk` placeholder, represent it as state
  metadata only, such as `capture_state`, synthetic `chunk_id`, and
  `duration_millis`. The first scaffold should not produce real chunks.

### Gesture Commands

- Represent gestures as explicit derived commands, not raw camera frames.
- Placeholder commands:
  - `gesture.air_draw`
  - `gesture.approve`
  - `gesture.reject`
- `gesture.air_draw` may include normalized points and a synthetic `gesture_id`.
- Approve/reject commands should be disabled unless paired, connected, and the
  UI has received a fake or host-provided staged-change-ready state.
- Do not request camera permission or add camera dependencies in this packet.

## Permission Gates

Use a small UI-facing permission gate model rather than direct platform capture:

```kotlin
enum class CaptureCapability { Touch, Ink, Audio, Gesture }

enum class PermissionGate {
    NotRequired,
    Required,
    Granted,
    Denied,
    DisabledByConnection,
    DisabledByFeatureFlag,
}
```

Recommended first-pass gates:

- Touch: `NotRequired` when connected; `DisabledByConnection` otherwise.
- Ink: `NotRequired` when connected; `DisabledByConnection` otherwise.
- Audio: `Required` or `Granted` only after explicit user start; forced to
  inactive on disconnect.
- Gesture: `DisabledByFeatureFlag` until a later camera/gesture packet chooses
  an implementation path.

Do not add `RECORD_AUDIO` or `CAMERA` manifest permissions unless the integrator
explicitly accepts a later implementation packet that includes visible user
controls and tests for inactive-on-disconnect behavior.

## Disabled-When-Disconnected Behavior

Capture enablement must derive from pairing and connection state:

```kotlin
data class CaptureConnectionGate(
    val paired: Boolean,
    val connected: Boolean,
    val stagedChangeReady: Boolean = false,
)

fun canCaptureTouch(gate: CaptureConnectionGate) = gate.paired && gate.connected
fun canCaptureInk(gate: CaptureConnectionGate) = gate.paired && gate.connected
fun canRequestAudio(gate: CaptureConnectionGate) = gate.paired && gate.connected
fun canSendGestureCommand(gate: CaptureConnectionGate) = gate.paired && gate.connected
fun canApproveOrReject(gate: CaptureConnectionGate) =
    gate.paired && gate.connected && gate.stagedChangeReady
```

Required reducer behavior:

- Unpaired, pairing, disconnected, and connecting states disable touch, ink,
  audio, and gesture commands.
- Any disconnect action forces audio to inactive and clears start/stop requests.
- Pending touch, ink, audio-state, and gesture placeholder events are dropped or
  marked unsent on disconnect.
- The companion must not buffer microphone or camera media while disconnected.
- UI controls must show inactive/disabled state rather than silently ignoring
  user input.

## Proposed Files

Use the package chosen by the Android scaffold integrator. File roles:

```text
app/src/main/java/.../capture/CaptureCapability.kt
app/src/main/java/.../capture/CapturePermissionGate.kt
app/src/main/java/.../capture/CaptureMode.kt
app/src/main/java/.../capture/CaptureState.kt
app/src/main/java/.../capture/CaptureAction.kt
app/src/main/java/.../capture/CaptureReducer.kt
app/src/main/java/.../capture/TouchCapturePlaceholder.kt
app/src/main/java/.../capture/InkCapturePlaceholder.kt
app/src/main/java/.../capture/AudioCaptureState.kt
app/src/main/java/.../capture/GestureCommandPlaceholder.kt
app/src/main/java/.../capture/InputEventFactory.kt
app/src/main/java/.../ui/state/CaptureUiState.kt
app/src/test/java/.../capture/CaptureReducerTest.kt
app/src/test/java/.../capture/InputEventFactoryTest.kt
```

Optional Compose integration points, if Agent 2/7 surfaces are ready:

```text
app/src/main/java/.../ui/components/CaptureControlStrip.kt
app/src/main/java/.../ui/components/InputSurfacePlaceholder.kt
```

## Acceptance Criteria

- Touch and ink placeholders can create schema-aligned input events with
  normalized coordinates and synthetic public-safe identifiers.
- Audio has explicit start/stop state and visible inactive state, but no raw
  recording, files, byte buffers, transcripts, or streaming implementation.
- Gesture placeholders emit derived command intent only and do not require camera
  permission.
- Capture is disabled when unpaired, pairing, disconnected, or connecting.
- Disconnection forces audio inactive and clears pending capture requests.
- Approve/reject gesture commands require a staged-change-ready flag.
- The companion never stores canonical whiteboard objects or host-resolved
  surface state.
- All examples and tests use synthetic IDs such as `session_synthetic_demo` and
  `surface_synthetic_hosted_whiteboard`.

## Verification Commands

Baseline repository checks:

```bash
python3 scripts/validate_companion_event.py examples/companion-event.example.json
git diff --check
```

Android checks after the scaffold exists:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Focused tests expected from this packet:

```bash
./gradlew :app:testDebugUnitTest --tests '*CaptureReducerTest'
./gradlew :app:testDebugUnitTest --tests '*InputEventFactoryTest'
```

Public-safety review prompts:

```bash
rg -n "RECORD_AUDIO|CAMERA|MediaRecorder|AudioRecord|CameraX|WebRTC|microphone buffer|audio bytes|video frame" app docs
rg -n "token|secret|password|api[_-]?key|credential|hostname|device_id|192\\.168\\.|10\\.|172\\.(1[6-9]|2[0-9]|3[0-1])\\." .
```

Intentional documentation references to forbidden categories should be manually
reviewed, not treated as automatic failures.

## Dependencies

- Agent 1 Android build scaffold.
- Agent 3 companion event models, or an equivalent event envelope.
- Agent 5 pairing/connection state projection for capture gating.
- Agent 7 normalized touch/ink mapper, if already accepted.
- Agent 6 fake transport only for deterministic local feedback; no real network
  dependency is required.
- Kotlin coroutines or Compose state may be used if already present in the app
  scaffold, but this packet does not require a new media, camera, WebRTC, QR, or
  networking dependency.

## Risks And Open Questions

- The current schema includes `audio.chunk`, but this packet should treat audio
  as state metadata until a later media packet defines explicit capture and
  retention behavior.
- Agent 7 may own some touch/ink mechanics. The integrator should keep Agent 8
  focused on capture gating and event-placeholder routing if overlap appears.
- Android runtime permission UX needs a later implementation decision before
  adding manifest permissions.
- Gesture support depends on whether recognition runs on-device, on the host, or
  both. This scaffold should only reserve derived-command shapes.
- The host protocol for staged-change readiness is not stable. Approve/reject
  should stay disabled unless fake feedback or host feedback provides that state.
