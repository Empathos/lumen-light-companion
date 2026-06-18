# Agent Packet Plan: Android Scaffold

## Purpose

This packet prepares `lumen-light-companion` for a coordinated Android scaffold
run using ten parallel agents and one serial integrator.

The goal is not to build a connected production companion. The goal is to create
a public-safe, reviewable Android application runway with a clean commit history,
clear contracts, synthetic local behavior, and no private deployment details.

## Process Source

Use `addyosmani/agent-skills` as the outside operating process for this run.
Do not vendor those skills into this repository.

Selected process skills:

- `using-agent-skills`: choose the applicable workflow.
- `spec-driven-development`: preserve the scaffold spec before code.
- `planning-and-task-breakdown`: split work into small verifiable units.
- `incremental-implementation`: keep each slice working and testable.
- `api-and-interface-design`: keep companion/host boundaries contract-first.
- `test-driven-development`: add proof around models and mappings.
- `git-workflow-and-versioning`: commit one logical change at a time.
- `code-review-and-quality`: review correctness, readability, architecture,
  security, and performance.
- `security-and-hardening`: protect public/private boundaries and media
  permission surfaces.
- `documentation-and-adrs`: document architectural decisions that survive the
  scaffold.

## Assumptions

1. The first Android scaffold is native Android with Kotlin and Jetpack Compose.
2. The scaffold is public generic upstream work, not a private local deployment.
3. The app may include fake/local session behavior, but no real host pairing.
4. Event contracts remain transport-independent.
5. WebSocket, WebRTC, and HTTP are interfaces or placeholders only until the
   hosted Lumen protocol stabilizes.
6. Android build verification depends on local toolchain availability.

## Non-Goals

- No real host URLs, device IDs, credentials, pairing records, logs, transcripts,
  screenshots, or deployment-specific configuration.
- No raw microphone or camera capture implementation in this scaffold.
- No live media streaming.
- No canonical whiteboard state on the companion.
- No agent-skill files or workflow machinery copied into the product.

## Source Material

Agents must treat these files as source of truth:

- `README.md`
- `AGENTS.md`
- `docs/PRD.md`
- `docs/ARCHITECTURE.md`
- `docs/ROADMAP.md`
- `schemas/companion-event.schema.json`
- `examples/companion-event.example.json`

## Global Agent Rules

Every agent must follow these rules:

- Work from public-safe assumptions only.
- Prefer small patch proposals over broad rewrites.
- Do not commit directly to `main`.
- Do not edit files outside the assigned packet unless the packet explicitly
  permits it.
- Do not add private hostnames, IP addresses, credentials, live logs, device
  identifiers, screenshots, or transcripts.
- Do not add agent-skills content to this repository.
- Do not invent host protocol details beyond synthetic local fixtures.
- Return a concise packet result containing files proposed, rationale,
  acceptance checks, and risks.

## Integration Model

Ten agents may work in parallel on draft packets. A single integrator applies
accepted work in the serialized commit order below.

Parallel agents produce:

- Proposed file additions or edits.
- Acceptance criteria.
- Verification commands.
- Risks and unresolved questions.

The integrator owns:

- Resolving overlaps.
- Running verification.
- Performing public-safety review.
- Creating all commits.
- Keeping commit history linear and meaningful.

## Ten Agent Assignments

### Agent 1: Android Build Scaffold

Scope:

- Root Gradle files.
- Android app module skeleton.
- Kotlin/Compose plugin configuration.
- Public-safe package name.

Expected output:

- Proposed `settings.gradle.kts`.
- Proposed root `build.gradle.kts`.
- Proposed `app/build.gradle.kts`.
- Proposed `app/src/main/AndroidManifest.xml`.

Forbidden:

- Real signing configuration.
- Private application IDs.
- Release deployment configuration.

Acceptance:

- Project structure is conventional Android/Kotlin.
- Debug build can be attempted with `./gradlew :app:assembleDebug`.
- No private deployment data appears in build files.

### Agent 2: Compose App Shell

Scope:

- Main activity.
- Compose theme.
- Top-level screen scaffold.
- Connection/session status placeholders.

Expected output:

- Main activity proposal.
- Minimal theme files.
- Basic screen layout with fake state.

Forbidden:

- Marketing landing page.
- Live network behavior.
- Private branding assets.

Acceptance:

- App shell communicates companion status, pairing state, and capture state.
- Text is operational and product-relevant.
- UI remains a tool surface, not a promotional page.

### Agent 3: Companion Event Models

Scope:

- Kotlin event domain models matching public schema concepts.
- Event type constants or sealed classes.
- Normalized coordinate models.

Expected output:

- Event model files.
- Notes on schema alignment.

Forbidden:

- Host-specific object model assumptions.
- Unbounded raw media model.

Acceptance:

- Models cover touch, ink, audio state, gesture command, and feedback events at
  scaffold level.
- Host ownership of canonical state remains explicit.

### Agent 4: Serialization And Fixtures

Scope:

- JSON serialization setup.
- Synthetic fixtures aligned with existing examples.
- Round-trip model tests.

Expected output:

- Serialization dependency recommendation.
- Fixture files.
- Unit test proposals.

Forbidden:

- Live session examples.
- Real user or device records.

Acceptance:

- Synthetic fixtures are public-safe.
- Round-trip tests prove event model shape.

### Agent 5: Pairing State Scaffold

Scope:

- Pairing state machine.
- Manual/QR pairing placeholders.
- Disconnected safe state.

Expected output:

- Pairing state models.
- Fake pairing reducer or controller.
- UI-facing state examples.

Forbidden:

- Real QR payload format.
- Authenticated device registry assumptions.
- Network hostnames or pairing secrets.

Acceptance:

- Unpaired and disconnected states stop capture.
- Pairing remains visibly user-mediated.

### Agent 6: Transport Boundary

Scope:

- Transport interface.
- Fake local transport implementation.
- Connection state events.

Expected output:

- Transport abstraction.
- Fake implementation for local UI/demo state.
- Tests for state transitions.

Forbidden:

- Real WebSocket URLs.
- WebRTC signaling.
- Background reconnect loops to unknown hosts.

Acceptance:

- App code depends on an interface, not a real network client.
- Fake transport supports deterministic tests.

### Agent 7: Touch And Ink Scaffold

Scope:

- Normalized touch mapper.
- Ink stroke model.
- Compose canvas placeholder.

Expected output:

- Coordinate normalization utilities.
- Ink stroke capture state.
- Canvas UI proposal.

Forbidden:

- Local canonical surface storage.
- Host object mutation logic.

Acceptance:

- Touch and ink emit input events only.
- Coordinates remain normalized and host-resolved.

### Agent 8: Haptics And Audio Permission Scaffold

Scope:

- Haptic pattern mapping from feedback events.
- Audio capture permission/status state.
- Explicit start/stop placeholders.

Expected output:

- Haptic mapping proposal.
- Audio status state proposal.
- Permission UI/state notes.

Forbidden:

- Raw audio recording implementation.
- Background capture.
- Hidden media state.

Acceptance:

- Haptics are driven by host feedback/fake feedback, not guessed canonical state.
- Audio capture is visibly inactive unless explicitly started.
- Disconnection forces audio state to inactive.

### Agent 9: Tests And Validation

Scope:

- Unit test strategy.
- Fixture validation.
- Public-safety scan command suggestions.

Expected output:

- Test file proposals.
- Verification command list.
- Public-safe scan checklist.

Forbidden:

- Tests requiring private services.
- Golden files from live sessions.

Acceptance:

- Tests cover event round-trip, pairing reducer, fake transport, coordinate
  mapping, and haptic mapping.
- Existing Python schema validation remains preserved.

### Agent 10: Documentation And Review Packet

Scope:

- Android scaffold spec.
- ADR for native Android + Compose.
- README/roadmap updates.
- Final review checklist.

Expected output:

- Documentation patch proposals.
- Review checklist.
- Known limitations.

Forbidden:

- Overstating production readiness.
- Internal operational notes that do not belong in the public repo.

Acceptance:

- Docs state the scaffold's honest status.
- Public/private boundary remains prominent.
- Next implementation steps are clear.

## Serialized Fourteen-Commit Plan

The integrator should apply accepted work in this order.

### Commit 1: `docs: add Android scaffold packet`

Purpose:

- Add this packet and align the run around a stable process.

Verify:

- `git diff --check`

### Commit 2: `docs: specify Android scaffold boundary`

Purpose:

- Add a concise Android scaffold spec derived from the PRD and architecture.

Likely files:

- `docs/ANDROID_SCAFFOLD_SPEC.md`

Verify:

- Spec covers objective, commands, structure, style, tests, boundaries, success
  criteria, and open questions.

### Commit 3: `docs: record native Android Compose decision`

Purpose:

- Add an ADR documenting native Android + Kotlin + Compose as the scaffold
  choice.

Likely files:

- `docs/adr/0001-native-android-compose.md`

Verify:

- ADR states context, decision, consequences, and alternatives.

### Commit 4: `build: add Android Gradle scaffold`

Purpose:

- Add Gradle settings, root build file, app build file, wrapper decision notes if
  needed, and minimal app module structure.

Likely files:

- `settings.gradle.kts`
- `build.gradle.kts`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`

Verify:

- `./gradlew :app:assembleDebug` if wrapper/toolchain exists.
- Otherwise record blocked verification with exact missing command/tool.

### Commit 5: `app: add Compose application shell`

Purpose:

- Add main activity, theme, and a minimal operational screen.

Likely files:

- `app/src/main/java/.../MainActivity.kt`
- `app/src/main/java/.../ui/...`

Verify:

- Build or static Kotlin/Gradle validation.
- UI text remains tool-surface oriented.

### Commit 6: `domain: add companion event models`

Purpose:

- Add Kotlin models for touch, ink, audio state, gesture command, feedback, and
  normalized coordinates.

Likely files:

- `app/src/main/java/.../domain/event/...`

Verify:

- Unit tests compile or model files are statically inspectable.
- Event names align with `docs/PRD.md`.

### Commit 7: `domain: add event serialization fixtures`

Purpose:

- Add serialization setup and synthetic fixture round-trip coverage.

Likely files:

- `app/src/test/...`
- `app/src/test/resources/...`

Verify:

- Round-trip tests pass if Android/JVM test tooling is available.
- Existing `python3 scripts/validate_companion_event.py examples/companion-event.example.json` still passes.

### Commit 8: `pairing: scaffold safe pairing state`

Purpose:

- Add fake/manual pairing state machine and disconnected safe state.

Likely files:

- `app/src/main/java/.../pairing/...`
- `app/src/test/.../pairing/...`

Verify:

- Tests prove unpaired/disconnected states disable capture.

### Commit 9: `transport: add fake companion transport`

Purpose:

- Add transport interface and deterministic fake transport.

Likely files:

- `app/src/main/java/.../transport/...`
- `app/src/test/.../transport/...`

Verify:

- Tests prove connection state transitions and fake event flow.

### Commit 10: `touch: add normalized touch and ink scaffold`

Purpose:

- Add normalized coordinate mapper, ink stroke state, and canvas placeholder.

Likely files:

- `app/src/main/java/.../input/...`
- `app/src/main/java/.../ui/...`
- `app/src/test/.../input/...`

Verify:

- Coordinate mapper tests pass.
- Touch and ink models emit events, not canonical surface state.

### Commit 11: `feedback: add haptic mapping scaffold`

Purpose:

- Add host-feedback-to-haptic-pattern mapping and fake invoker boundary.

Likely files:

- `app/src/main/java/.../feedback/...`
- `app/src/test/.../feedback/...`

Verify:

- Tests map known feedback events to named patterns.
- No haptic behavior claims host state locally.

### Commit 12: `audio: scaffold explicit capture state`

Purpose:

- Add microphone permission/status state and explicit start/stop placeholder.

Likely files:

- `app/src/main/java/.../audio/...`
- `app/src/test/.../audio/...`

Verify:

- Tests prove audio state is inactive by default and inactive after disconnect.
- No raw audio persistence or streaming is added.

### Commit 13: `test: add scaffold verification suite`

Purpose:

- Consolidate unit tests, fixture tests, and public-safety scan guidance.

Likely files:

- `app/src/test/...`
- `scripts/public_safety_scan.py` if needed
- `prompts/public-safety-scan.prompt.md` if adding a script

Verify:

- Android tests if toolchain exists.
- Existing Python validation passes.
- Public-safety scan finds no private markers.

### Commit 14: `docs: update Android scaffold status`

Purpose:

- Update README/roadmap with honest scaffold status and next steps.

Likely files:

- `README.md`
- `docs/ROADMAP.md`

Verify:

- Docs do not claim live host pairing, real media transport, or production
  readiness.

## Checkpoints

### Checkpoint A: After Commit 3

- The scaffold has a written Android spec.
- The stack decision is documented.
- No Android code has been added yet.

### Checkpoint B: After Commit 7

- The app can be built or the build blocker is recorded.
- Event models and serialization are present.
- Existing schema validation still passes.

### Checkpoint C: After Commit 12

- Pairing, fake transport, touch/ink, haptics, and audio state scaffolds exist.
- No raw media capture or live network behavior exists.
- Disconnected state disables capture surfaces.

### Checkpoint D: After Commit 14

- All available tests pass.
- Public-safety scan passes.
- README and roadmap truthfully describe scaffold status.
- Commit history is linear and each commit has one logical purpose.

## Verification Commands

Baseline commands:

```bash
python3 scripts/validate_companion_event.py examples/companion-event.example.json
git diff --check
```

Android commands, if Gradle wrapper/toolchain exists:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Public-safety checks:

```bash
rg -n "token|secret|password|api[_-]?key|credential|session_id|device_id|localhost|192\\.168\\.|10\\.|172\\.(1[6-9]|2[0-9]|3[0-1])\\." .
rg -n "tailnet|tailscale|\\.ts\\.net|transcript|screenshot|recording" .
```

The public-safety checks may produce intentional matches in documentation that
names forbidden categories. Treat those as review prompts, not automatic failure.

## Final Review Checklist

- [ ] No agent-skills files were copied into this repository.
- [ ] No private deployment data was added.
- [ ] No live host URL, device ID, credential, transcript, screenshot, or log was
      added.
- [ ] The companion remains an input and feedback layer.
- [ ] The host remains canonical owner of session state.
- [ ] Media capture is explicit, visible, and scaffold-only.
- [ ] Every commit is atomic and independently understandable.
- [ ] Build/test verification is recorded honestly.
- [ ] README and roadmap state that this is a scaffold, not a live companion.

## Open Questions For Human Review

- Should the first scaffold include a Gradle wrapper, or rely on the local
  Android/Gradle toolchain?
- Should the package name use `ai.empathos.lumen.companion` or a more generic
  public namespace?
- Should fake transport live entirely in test code, or power a local demo mode in
  the debug app?
- Should microphone permission UI be present in the first scaffold if raw audio
  capture remains intentionally unimplemented?
