# Agent 2 Draft: Compose App Shell

## Scope

This packet proposes the first Android Compose shell for Lumen Light Companion.
It should be applied after the Android Gradle scaffold exists and before deeper
domain, pairing, transport, input, haptics, or audio implementation packets.

The shell must stay public-safe and synthetic. It is a companion tool surface for
a hosted Lumen Light session, not a phone whiteboard and not a marketing page.

## Proposed Files

- `app/src/main/java/ai/empathos/lumen/companion/MainActivity.kt`
  - Compose entry point.
  - Sets the app theme.
  - Supplies static fake state for the first shell.
- `app/src/main/java/ai/empathos/lumen/companion/ui/LumenCompanionApp.kt`
  - Top-level `Scaffold`.
  - Owns the screen composition and preview-friendly default state.
- `app/src/main/java/ai/empathos/lumen/companion/ui/state/CompanionShellState.kt`
  - UI-only state model for connection, pairing, capture, recording, and latest
    feedback.
  - Uses generic labels and synthetic identifiers only.
- `app/src/main/java/ai/empathos/lumen/companion/ui/theme/Color.kt`
  - Minimal public app color tokens.
- `app/src/main/java/ai/empathos/lumen/companion/ui/theme/Theme.kt`
  - Material 3 theme wrapper.
- `app/src/main/java/ai/empathos/lumen/companion/ui/theme/Type.kt`
  - Typography setup if the scaffold needs an explicit file.
- `app/src/main/java/ai/empathos/lumen/companion/ui/components/StatusPill.kt`
  - Reusable compact status row element for paired, connected, capture, and
    recording status.
- `app/src/main/java/ai/empathos/lumen/companion/ui/screens/CompanionShellScreen.kt`
  - Operational screen content: status, pairing placeholder, capture controls,
    feedback, and input surface placeholder.

If the integrator chooses a different package name in Agent 1, keep the same
file roles under that package.

## Screen States

Define static UI states only. Later packets can replace these with reducers,
transport state, and permission models.

- `Unpaired`
  - Pairing state: not paired.
  - Connection state: disconnected.
  - Capture state: disabled.
  - Audio state: inactive.
  - Primary action: "Pair with host".
- `PairingReady`
  - Pairing state: manual or QR placeholder visible.
  - Connection state: waiting.
  - Capture state: disabled.
  - Audio state: inactive.
  - Primary action: "Cancel pairing".
- `PairedDisconnected`
  - Pairing state: paired with synthetic session label.
  - Connection state: disconnected.
  - Capture state: disabled.
  - Audio state: forced inactive.
  - Primary action: "Reconnect" placeholder.
- `PairedConnectedIdle`
  - Pairing state: paired with synthetic session label.
  - Connection state: connected through fake/local state.
  - Capture state: touch ready, ink ready, audio inactive.
  - Latest feedback: none or last synthetic host status.
- `TouchActive`
  - Connection state: connected.
  - Capture state: touch or ink active.
  - Audio state: inactive unless a later audio packet explicitly starts it.
  - Surface area shows normalized input placeholder, not local canonical objects.
- `RecordingPlaceholder`
  - Connection state: connected.
  - Audio state: requested or active placeholder only.
  - Must remain visibly user-started and stoppable.
  - No raw audio recording or streaming implementation.
- `HostFeedbackReceived`
  - Latest feedback event shown as a concise status, such as "Selection changed"
    or "Change accepted".
  - Haptic behavior remains a placeholder until the feedback packet.

## UI Structure

- Top app bar
  - Title: `Lumen Companion`.
  - Subtitle or compact status text: `Hosted session input`.
  - No promotional copy.
- Session status band
  - Pairing state, connection state, and hosted session label.
  - Use synthetic labels such as `session_synthetic_demo`; never private session
    IDs or hostnames.
- Capture status row
  - Touch, ink, microphone, and feedback status pills.
  - Disconnected or unpaired state visibly disables capture.
- Input surface placeholder
  - A bounded Compose area labeled as the companion input surface.
  - Shows normalized-coordinate affordance only.
  - Does not render or store canonical whiteboard objects.
- Control strip
  - Pair/unpair placeholder action.
  - Touch/ink mode placeholders.
  - Audio start/stop placeholder disabled until paired and connected.
  - Review controls for approve/reject may appear disabled or as future-ready
    placeholders if screen space allows.
- Feedback panel
  - Latest host feedback/status event.
  - Synthetic event type examples may use schema names like
    `feedback.haptic`, `surface.selection_changed`, or
    `session.connection_changed`.

Use Material 3 Compose primitives. Keep cards compact and task-oriented. Avoid a
hero section, app marketing, private branding assets, screenshots, live host
status, or network configuration.

## Acceptance Criteria

- The app launches to an operational companion shell.
- The shell communicates pairing state, connection state, capture availability,
  audio/recording state, and latest feedback state.
- Unpaired or disconnected state visibly disables capture and audio controls.
- UI copy reinforces that the hosted Lumen session owns canonical state.
- The input surface is a control/capture placeholder, not a local whiteboard.
- State is fake, local, deterministic, and public-safe.
- No live network calls, WebSocket clients, WebRTC setup, HTTP clients, pairing
  secrets, real QR payloads, hostnames, IP addresses, device IDs, screenshots,
  transcripts, credentials, or private assets are added.
- The shell can be previewed or launched without private configuration.
- Compose files are small enough for later agents to replace state sources
  without rewriting the screen from scratch.

## Verification Commands

Baseline repo checks:

```bash
python3 scripts/validate_companion_event.py examples/companion-event.example.json
git diff --check
```

Android checks, once Agent 1 has added a build scaffold and local toolchain
support exists:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Public-safety review prompts:

```bash
rg -n "token|secret|password|api[_-]?key|credential|session_id|device_id|localhost|192\\.168\\.|10\\.|172\\.(1[6-9]|2[0-9]|3[0-1])\\." .
rg -n "tailnet|tailscale|\\.ts\\.net|transcript|screenshot|recording" .
```

Expected intentional matches may appear in docs or schema examples. Review them
as prompts; do not treat documentation of forbidden categories as automatic
failure.

## Dependencies

- Agent 1 Android build scaffold.
- Kotlin Android plugin and Jetpack Compose configured in Gradle.
- AndroidX Activity Compose.
- Compose Material 3.
- Compose tooling preview/debug dependencies if the build scaffold permits.

No network, media, camera, haptic, QR, serialization, or permission dependency is
required for this packet.

## Risks And Open Questions

- Package name depends on Agent 1. Use the build scaffold's selected public-safe
  namespace.
- If microphone permission UI appears too early, users may infer working audio
  capture. Prefer disabled or clearly inactive controls until the audio packet.
- Review controls could imply host staging behavior that does not exist yet.
  Keep approve/reject disabled or visibly placeholder-only.
- Static fake state is useful for shell review but should be replaced by later
  reducer and transport packets before real interaction work.
- Public-safety scans may flag schema field names like `session_id` and
  documentation references to forbidden categories. The integrator should review
  these manually.
