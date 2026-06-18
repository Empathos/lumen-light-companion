# Agent 7 Draft Packet: Haptics And Feedback Mapping

## Scope

This packet proposes the public-safe feedback-to-haptics scaffold for the
Android app. It covers host feedback event mapping, named haptic pattern
constants, fake feedback fixtures, an invoker boundary, accessibility controls,
silent/degraded modes, acceptance criteria, verification commands, dependencies,
and risks.

The companion remains an input and feedback surface. Haptics are driven by host
feedback events or deterministic fake feedback fixtures. The companion must not
guess canonical surface state from local touch, ink, audio, or pairing state.

Note: `docs/AGENT_PACKET_PLAN.md` lists haptics under Agent 8, while this
assigned packet requested Agent 7. This draft follows the assigned scope and
uses the requested filename.

## Feedback Event Mapping

Map public schema event types to stable app-level haptic pattern names:

| Incoming event | Payload cue | Haptic pattern |
| --- | --- | --- |
| `feedback.haptic` | `pattern=selection_move_tick` | `SelectionMoveTick` |
| `feedback.haptic` | `pattern=snap_alignment_double_tick` | `SnapAlignmentDoubleTick` |
| `feedback.haptic` | `pattern=boundary_collision_reject` | `BoundaryCollisionReject` |
| `surface.selection_changed` | any host selection change | `SelectionChangedTick` |
| `surface.staged_change_ready` | staged review is ready | `StagedChangeReadyBuzz` |
| `surface.change_accepted` | host accepted staged change | `ChangeAcceptedFirmTick` |
| `surface.change_rejected` | host rejected staged change | `ChangeRejectedSoftReject` |
| `session.recording_started` | explicit host recording state | `RecordingStartedPulse` |
| `session.recording_stopped` | explicit host recording state | `RecordingStoppedSettle` |
| `session.connection_changed` | `status=connected` | `ConnectionRestoredTick` |
| `session.connection_changed` | `status=disconnected` or `status=lost` | `PairingLostFade` |
| any event | haptics disabled, unavailable, or unknown pattern | `None` |

Rules:

- Prefer explicit `feedback.haptic.payload.pattern` when present and recognized.
- Use status event mappings only for host-origin events with `device_role=host`.
- Unknown event types or unknown payload patterns must be ignored.
- Silent mode, reduced feedback mode, missing vibrator hardware, failed pairing,
  or disconnection-safe suppression maps every event to `None`.
- Do not play haptics from local predictions such as "the user probably snapped
  an object"; wait for host feedback or a fake host fixture.
- Rate-limit repeated tick-class patterns so high-frequency move feedback does
  not become distracting. A first scaffold can use a simple minimum interval
  such as 40-75 ms for `SelectionMoveTick`.

## Haptic Pattern Constants

Suggested Kotlin shape:

```kotlin
enum class HapticPattern(val wireName: String) {
    SelectionChangedTick("selection_changed_tick"),
    SelectionMoveTick("selection_move_tick"),
    SnapAlignmentDoubleTick("snap_alignment_double_tick"),
    StagedChangeReadyBuzz("staged_change_ready_buzz"),
    ChangeAcceptedFirmTick("change_accepted_firm_tick"),
    ChangeRejectedSoftReject("change_rejected_soft_reject"),
    BoundaryCollisionReject("boundary_collision_reject"),
    RecordingStartedPulse("recording_started_pulse"),
    RecordingStoppedSettle("recording_stopped_settle"),
    PairingLostFade("pairing_lost_fade"),
    ConnectionRestoredTick("connection_restored_tick"),
    None("none"),
}
```

Suggested Android mapping:

| Pattern | Primary platform mapping | Fallback |
| --- | --- | --- |
| `SelectionChangedTick` | `HapticFeedbackType.TextHandleMove` or light tick | no-op |
| `SelectionMoveTick` | light tick with rate limit | no-op |
| `SnapAlignmentDoubleTick` | double click or two short ticks | single tick |
| `StagedChangeReadyBuzz` | short low-amplitude waveform | click |
| `ChangeAcceptedFirmTick` | heavy click | click |
| `ChangeRejectedSoftReject` | short-short waveform | click |
| `BoundaryCollisionReject` | reject waveform | click |
| `RecordingStartedPulse` | medium pulse | click |
| `RecordingStoppedSettle` | soft single tick | no-op |
| `PairingLostFade` | descending/longer waveform where supported | click or no-op |
| `ConnectionRestoredTick` | single click | no-op |
| `None` | no-op | no-op |

Keep platform details behind an interface so tests verify selected pattern names
without invoking real device vibration:

```kotlin
interface HapticFeedbackInvoker {
    fun perform(pattern: HapticPattern)
}
```

## Accessibility And Silent Modes

Add a small UI-facing preference/state model before wiring device vibration:

```kotlin
enum class HapticsMode {
    SystemDefault,
    Enabled,
    Reduced,
    Silent,
}
```

Required behavior:

- `Silent` never invokes vibration, but status text may still update.
- `Reduced` permits only meaningful low-frequency state transitions:
  acceptance, rejection, recording start/stop, pairing lost, and connection
  restored.
- `SystemDefault` should respect Android/system haptic settings where platform
  APIs expose them.
- Missing vibrator hardware degrades to `None` without errors.
- App UI must expose haptic status plainly, such as enabled, reduced, silent, or
  unavailable. Do not hide degraded mode.
- Do not use haptics as the only status channel; visible status remains required
  for accessibility and silent environments.

## Fake Feedback Fixtures

Add deterministic host-origin fixtures only. Use synthetic IDs already aligned
with the existing public schema:

```text
app/src/test/resources/fixtures/feedback/feedback-haptic-selection-move.json
app/src/test/resources/fixtures/feedback/surface-selection-changed.json
app/src/test/resources/fixtures/feedback/surface-staged-change-ready.json
app/src/test/resources/fixtures/feedback/surface-change-accepted.json
app/src/test/resources/fixtures/feedback/surface-change-rejected.json
app/src/test/resources/fixtures/feedback/session-recording-started.json
app/src/test/resources/fixtures/feedback/session-recording-stopped.json
app/src/test/resources/fixtures/feedback/session-connection-lost.json
app/src/test/resources/fixtures/feedback/session-connection-restored.json
```

Example fixture shape:

```json
{
  "event_id": "event_synthetic_feedback_selection_move_001",
  "event_type": "feedback.haptic",
  "session_id": "session_synthetic_demo",
  "surface_id": "surface_synthetic_hosted_whiteboard",
  "device_role": "host",
  "timestamp": "2026-01-01T00:00:01Z",
  "sequence": 18,
  "payload": {
    "pattern": "selection_move_tick",
    "reason": "synthetic_selection_move"
  }
}
```

Do not include hostnames, IPs, real session IDs, real device IDs, transcripts,
audio payloads, screenshots, logs, credentials, or deployment configuration.

## Proposed Files

- `app/src/main/java/org/lumen/lightcompanion/feedback/HapticPattern.kt`
  - Pattern enum and wire-name parser.
- `app/src/main/java/org/lumen/lightcompanion/feedback/HapticsMode.kt`
  - User/system haptic mode and reduced/silent policy.
- `app/src/main/java/org/lumen/lightcompanion/feedback/FeedbackHapticMapper.kt`
  - Pure function mapping host feedback/status events to `HapticPattern`.
- `app/src/main/java/org/lumen/lightcompanion/feedback/HapticFeedbackInvoker.kt`
  - Boundary for Android haptic execution.
- `app/src/main/java/org/lumen/lightcompanion/feedback/AndroidHapticFeedbackInvoker.kt`
  - Thin platform implementation, added only after the Compose/activity scaffold
    exists.
- `app/src/main/java/org/lumen/lightcompanion/feedback/FakeHapticFeedbackInvoker.kt`
  - Test/demo invoker that records performed patterns.
- `app/src/test/java/org/lumen/lightcompanion/feedback/FeedbackHapticMapperTest.kt`
  - Deterministic unit tests for event-to-pattern behavior.
- `app/src/test/resources/fixtures/feedback/*.json`
  - Synthetic host-origin feedback fixtures.

If Agent 1 chooses a different public package, keep the same file roles under
that package.

## Acceptance Criteria

- Host-origin feedback/status events map to the expected haptic pattern names.
- `feedback.haptic.payload.pattern` is honored only when recognized.
- Unknown patterns, companion-origin events, disabled haptics, unavailable
  haptics, or disconnected-safe suppression produce `HapticPattern.None`.
- Reduced mode suppresses high-frequency movement ticks but preserves important
  state transitions.
- Silent mode never invokes the platform haptic boundary.
- Tests use fake invokers and synthetic fixtures; they do not require hardware.
- No real host protocol, pairing secret, device identifier, live log, transcript,
  audio, screenshot, credential, or deployment detail is introduced.
- Haptic mapping does not mutate canonical surface state or infer host outcomes
  from local touch/ink input.

## Verification Commands

Run when the Android scaffold and fixtures exist:

```bash
./gradlew :app:testDebugUnitTest --tests '*FeedbackHapticMapperTest'
./gradlew :app:testDebugUnitTest
python3 scripts/validate_companion_event.py examples/companion-event.example.json
for f in app/src/test/resources/fixtures/feedback/*.json; do python3 scripts/validate_companion_event.py "$f"; done
rg -n "http://|https://|[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+|password|secret|token|credential|device_[A-Za-z0-9_-]+|transcript|screenshot|audio/[A-Za-z0-9.+-]+" app/src/main app/src/test app/src/test/resources docs/agent-drafts || true
git diff --check
```

Expected validator output for schema-valid fixtures:

```text
LUMEN_LIGHT_COMPANION_EVENT_OK
```

## Dependencies

- Agent 3 event models or a temporary generic companion event model.
- Agent 4 serialization fixtures if tests load JSON fixtures.
- Agent 5 pairing state for disconnected-safe suppression.
- Agent 6 transport/fake transport for host-origin fake feedback flow.
- Android haptic APIs through Compose `LocalHapticFeedback` and/or
  `VibratorManager`/`VibrationEffect`, chosen by the integrator after the app
  scaffold exists.
- Android `VIBRATE` permission only if the implementation uses vibrator service
  APIs that require it. Prefer the least-permission approach that satisfies the
  chosen platform mapping.

## Risks And Open Questions

- The current schema leaves `payload` open, so tests must enforce recognized
  haptic pattern names until the schema becomes stricter.
- Android devices vary widely in haptic strength and supported predefined
  effects; mappings must degrade without failing.
- Accessibility expectations may require a more explicit setting than
  `SystemDefault` if users want companion haptics off while system haptics remain
  on.
- High-frequency movement feedback can become noisy; rate limits and reduced
  mode should be tested early on real devices in private downstream work.
- `PairingLostFade` may not be physically representable on every device; the
  first implementation should treat it as a best-effort pattern, not a contract
  for exact tactile feel.
- Final event names for snap alignment and collision may move from
  `feedback.haptic.payload.pattern` into more specific host status events later.
