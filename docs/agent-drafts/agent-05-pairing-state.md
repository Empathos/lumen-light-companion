# Agent 5 Draft: Pairing State Scaffold

## Scope

This packet proposes the public-safe pairing state scaffold for the Android app.
It defines UI-facing state, a fake reducer/controller, manual and QR placeholder
flows, and disconnected-safe behavior. It does not implement real pairing,
authentication, transport, QR payload parsing, device registry lookup, host URLs,
or secret handling.

The companion remains an input and feedback surface. The hosted Lumen session
owns pairing approval, canonical session state, surface state, and feedback.

## Proposed Files

- `app/src/main/java/org/lumen/lightcompanion/pairing/PairingState.kt`
  - Domain state for unpaired, pairing, paired, connected, disconnected, and
    failed placeholder states.
  - Public-safe synthetic session and surface labels only.
- `app/src/main/java/org/lumen/lightcompanion/pairing/PairingAction.kt`
  - User and fake host actions consumed by the reducer.
- `app/src/main/java/org/lumen/lightcompanion/pairing/PairingReducer.kt`
  - Pure reducer for deterministic local pairing state transitions.
- `app/src/main/java/org/lumen/lightcompanion/pairing/FakePairingController.kt`
  - Preview/demo controller that dispatches reducer actions without network IO.
- `app/src/main/java/org/lumen/lightcompanion/ui/state/PairingUiState.kt`
  - UI-facing projection used by Agent 2's Compose shell.
- `app/src/test/java/org/lumen/lightcompanion/pairing/PairingReducerTest.kt`
  - Unit tests for disconnected-safe and capture-gating behavior.

If the integrator chooses a different public-safe package in Agent 1, keep the
same file roles under that package.

## State Machine

The first scaffold should use a small explicit state machine:

```text
Unpaired
  -> ManualEntryOpen
  -> QrScanPlaceholder

ManualEntryOpen
  -> PairingRequested
  -> Unpaired

QrScanPlaceholder
  -> PairingRequested
  -> Unpaired

PairingRequested
  -> PairedDisconnected
  -> PairingFailed
  -> Unpaired

PairedDisconnected
  -> PairedConnecting
  -> Unpaired

PairedConnecting
  -> PairedConnected
  -> PairedDisconnected
  -> Unpaired

PairedConnected
  -> PairedDisconnected
  -> Unpaired
```

Suggested state definitions:

```kotlin
sealed interface PairingState {
    data object Unpaired : PairingState

    data class ManualEntryOpen(
        val enteredCode: String = "",
    ) : PairingState

    data object QrScanPlaceholder : PairingState

    data class PairingRequested(
        val method: PairingMethod,
        val displayLabel: String,
    ) : PairingState

    data class PairedDisconnected(
        val sessionLabel: String,
        val surfaceLabel: String,
        val reason: DisconnectReason,
    ) : PairingState

    data class PairedConnecting(
        val sessionLabel: String,
        val surfaceLabel: String,
    ) : PairingState

    data class PairedConnected(
        val sessionLabel: String,
        val surfaceLabel: String,
        val canCaptureTouch: Boolean = true,
        val canCaptureInk: Boolean = true,
        val canCaptureAudio: Boolean = false,
    ) : PairingState

    data class PairingFailed(
        val method: PairingMethod,
        val message: String,
    ) : PairingState
}
```

Supporting enums should stay generic:

```kotlin
enum class PairingMethod { ManualCode, QrPlaceholder }
enum class DisconnectReason { NotConnected, UserDisconnected, HostUnavailable, PairingLost }
```

Use synthetic labels such as `session_synthetic_demo` and
`surface_synthetic_hosted_whiteboard` when a paired placeholder needs display
context. These labels are not secrets and must not imply a real host.

## Actions And Reducer

Actions should represent local user intent and fake host/session feedback:

```kotlin
sealed interface PairingAction {
    data object OpenManualEntry : PairingAction
    data object OpenQrPlaceholder : PairingAction
    data class ManualCodeChanged(val value: String) : PairingAction
    data object SubmitManualPlaceholder : PairingAction
    data object SubmitQrPlaceholder : PairingAction
    data object CancelPairing : PairingAction
    data object FakeHostApproved : PairingAction
    data object FakeHostRejected : PairingAction
    data object ConnectRequested : PairingAction
    data object FakeTransportConnected : PairingAction
    data class FakeTransportDisconnected(val reason: DisconnectReason) : PairingAction
    data object UnpairRequested : PairingAction
}
```

Reducer rules:

- `OpenManualEntry` and `OpenQrPlaceholder` are valid only from `Unpaired` or
  `PairingFailed`.
- `SubmitManualPlaceholder` and `SubmitQrPlaceholder` move to
  `PairingRequested`; they do not parse or transmit real payloads.
- `FakeHostApproved` moves to `PairedDisconnected` with synthetic labels.
- `ConnectRequested` moves from `PairedDisconnected` to `PairedConnecting`.
- `FakeTransportConnected` moves from `PairedConnecting` to `PairedConnected`.
- `FakeTransportDisconnected` always moves any paired state to
  `PairedDisconnected`.
- `UnpairRequested` always returns to `Unpaired` and clears display labels.
- Invalid actions should be no-ops so previews and tests remain deterministic.

## Manual And QR Placeholders

Manual pairing placeholder:

- Show a short-code entry field or disabled review field in UI.
- Accept only a synthetic value such as `DEMO-CODE` in fake/demo mode.
- Never document or infer a real code length, entropy model, expiration rule, or
  authentication format in this public scaffold.
- Do not persist entered values.

QR pairing placeholder:

- Show a scanner placeholder surface and a manual "Use demo QR" action.
- Do not add a camera permission in this packet.
- Do not add QR scanning dependencies in this packet unless the integrator
  explicitly chooses to install a fake-only QR UI dependency later.
- Do not define a real QR payload format. If a sample is needed, use a literal
  non-transport placeholder label such as `qr_synthetic_pairing_placeholder`.

Both flows should be visibly user-mediated. The app must never silently pair on
launch or auto-connect to an implied host.

## Disconnected-Safe Behavior

Capture gating must derive from pairing and connection state:

```kotlin
val isPaired = state is PairedDisconnected ||
    state is PairedConnecting ||
    state is PairedConnected

val isConnected = state is PairedConnected
val canCaptureTouch = state is PairedConnected && state.canCaptureTouch
val canCaptureInk = state is PairedConnected && state.canCaptureInk
val canCaptureAudio = state is PairedConnected && state.canCaptureAudio
```

Required behavior:

- `Unpaired`, `ManualEntryOpen`, `QrScanPlaceholder`, `PairingRequested`, and
  `PairingFailed` stop all capture.
- `PairedDisconnected` and `PairedConnecting` stop all capture, including audio.
- Any fake disconnect action forces touch, ink, microphone, and camera/gesture
  capture to disabled or inactive UI state.
- Audio remains disabled by default even in `PairedConnected`; the audio packet
  must add explicit user start/stop behavior before any active microphone state.
- Camera/gesture controls remain absent or disabled because this packet does not
  request camera permission or define raw media behavior.
- Pending local input events should be dropped or marked unsent when disconnect
  occurs. The companion must not buffer media while disconnected.

## UI-Facing State Examples

`PairingUiState` should flatten reducer state for Compose:

```kotlin
data class PairingUiState(
    val title: String,
    val sessionLabel: String?,
    val surfaceLabel: String?,
    val pairingStatus: String,
    val connectionStatus: String,
    val primaryActionLabel: String,
    val secondaryActionLabel: String?,
    val manualEntryVisible: Boolean,
    val qrPlaceholderVisible: Boolean,
    val captureEnabled: Boolean,
    val audioEnabled: Boolean,
    val safetyMessage: String?,
)
```

Example projections:

- `Unpaired`
  - Pairing status: `Not paired`
  - Connection status: `Disconnected`
  - Capture enabled: `false`
  - Primary action: `Pair with host`
- `ManualEntryOpen`
  - Pairing status: `Manual pairing`
  - Connection status: `Waiting for user`
  - Capture enabled: `false`
- `QrScanPlaceholder`
  - Pairing status: `QR pairing placeholder`
  - Connection status: `Waiting for user`
  - Capture enabled: `false`
- `PairedDisconnected`
  - Pairing status: `Paired`
  - Connection status: `Disconnected`
  - Capture enabled: `false`
  - Safety message: `Capture is stopped while disconnected.`
- `PairedConnected`
  - Pairing status: `Paired`
  - Connection status: `Connected`
  - Capture enabled: `true`
  - Audio enabled: `false`

Keep copy operational and neutral. Do not include hostnames, private session
names, screenshots, transcripts, live logs, or deployment labels.

## Acceptance Criteria

- Pairing state is represented by explicit sealed models or equivalent small
  types.
- Manual and QR flows are placeholders only and remain user-mediated.
- No real QR payload format, auth secret, pairing token, host URL, network
  hostname, IP address, device identifier, registry assumption, or deployment
  data is added.
- Unpaired, pairing, failed, disconnected, and connecting states disable all
  capture.
- Fake disconnect from any paired state forces capture and audio off.
- Audio is off by default even when the fake session is connected.
- State transitions are deterministic and testable without Android framework,
  network, media, or camera dependencies.
- UI-facing state uses synthetic labels only, such as `session_synthetic_demo`
  and `surface_synthetic_hosted_whiteboard`.
- The scaffold preserves host ownership of canonical pairing approval and
  whiteboard/session state.

## Verification Commands

Baseline checks:

```bash
python3 scripts/validate_companion_event.py examples/companion-event.example.json
git diff --check
```

Android checks after Agent 1 and relevant model/test packets are integrated:

```bash
./gradlew :app:testDebugUnitTest --tests '*PairingReducerTest'
./gradlew :app:assembleDebug
```

Public-safety review:

```bash
rg -n "https?://|[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+|token|secret|password|credential|api[_-]?key|device_[A-Za-z0-9_-]+|host_[A-Za-z0-9_-]+" .
rg -n "qr_.*payload|pairing.*payload|registry|transcript|screenshot|recording" .
```

Expected intentional matches may appear in documentation about forbidden
categories or synthetic examples. Review matches manually.

## Dependencies

- Agent 1 Android build scaffold.
- Agent 2 Compose shell if UI projections are wired into the visible app.
- Agent 3 companion event models for later alignment with session and surface
  labels.
- Kotlin standard library only for the reducer/model layer.
- JUnit or the repo's chosen unit test framework for reducer tests.

No network, WebSocket, WebRTC, HTTP, camera, microphone, QR scanner, credential
storage, Android permission, or device registry dependency is required for this
packet.

## Risks And Open Questions

- Real pairing security is intentionally out of scope. This scaffold should not
  imply entropy, expiration, approval, identity, or registry behavior.
- Agent 6's transport boundary will need to decide how fake connection events
  feed this reducer without leaking transport-specific assumptions into pairing
  models.
- Agent 7's permission gating should reuse the disconnected-safe rule instead
  of allowing media capture from a stale permission state.
- If QR UI is added too early, reviewers may infer camera scanning exists.
  Prefer a static QR placeholder until the media/permission packets are ready.
- Package naming may differ across drafts. The integrator should normalize all
  proposed files to the package chosen by the Android scaffold.
