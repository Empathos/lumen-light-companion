# Agent 10 Draft Packet: Security, Privacy, And Public-Safety Review

## Scope

Review the Android scaffold boundary before integration. This packet covers
threat model notes, Android permission risk checks, public/private leakage scan
patterns, proposed review files, acceptance criteria, verification commands,
dependencies, and unresolved security questions.

This review is intentionally scaffold-level. It does not define live pairing,
authentication, host URLs, media streaming, retention policy, or deployment
configuration.

## Threat Model For Scaffold Boundaries

### Assets

- Public companion event contracts and synthetic fixtures.
- User-visible pairing, connection, permission, recording, and feedback state.
- Local touch, ink, microphone, camera/gesture, and haptic state.
- Host-owned canonical surface state, transcript state, artifacts, and memory
  output.
- Private downstream pairing records, host addresses, credentials, logs, media,
  device identifiers, and deployment settings.

### Trust Boundaries

- Android companion process to hosted Lumen session transport.
- Companion UI state to Android runtime permissions.
- Companion event model to host event router.
- Synthetic public fixture data to private downstream live data.
- Public repo scaffold to private downstream implementation.

### Primary Risks

- The companion accidentally becomes a local canonical whiteboard by caching or
  owning durable surface state.
- Public examples or tests include real hostnames, pairing records, device IDs,
  logs, transcripts, screenshots, or deployment-specific configuration.
- Android permissions are requested before a visible user action and paired
  session state exist.
- Microphone or camera capture continues after unpairing, disconnect, background
  transition, or permission revocation.
- `payload` remains too permissive and accepts raw media, transcript text,
  binary blobs, filenames, URLs, or host-specific implementation details.
- Haptic feedback is guessed locally instead of derived from host feedback
  events, causing misleading privacy or state signals.
- Future transport code hardcodes local network details, auth secrets, or
  development endpoints into public source.

### Scaffold Controls

- Keep host ownership explicit in docs, models, reducers, and UI copy.
- Use synthetic IDs only, with obvious prefixes such as `synthetic`, `demo`, or
  `example`.
- Avoid real network, WebSocket, WebRTC, microphone, camera, QR scanner, storage,
  notification, and background-service code in the first scaffold unless a later
  packet explicitly owns it.
- Model microphone and camera as permission/status state first, not capture
  buffers or media files.
- Treat media events as metadata or derived-command placeholders until the host
  protocol and retention model are defined.
- Keep all private deployment values in downstream private repos or branches.

## Android Permission Risk Checks

The first Android scaffold should request no dangerous permissions by default.

Require explicit review before adding any of:

```text
android.permission.CAMERA
android.permission.RECORD_AUDIO
android.permission.INTERNET
android.permission.VIBRATE
android.permission.POST_NOTIFICATIONS
android.permission.FOREGROUND_SERVICE
android.permission.READ_MEDIA_AUDIO
android.permission.READ_MEDIA_IMAGES
android.permission.READ_MEDIA_VIDEO
android.permission.READ_EXTERNAL_STORAGE
android.permission.WRITE_EXTERNAL_STORAGE
android.permission.ACCESS_NETWORK_STATE
android.permission.ACCESS_WIFI_STATE
```

Permission acceptance checks:

- No permission is added just to support fake local behavior.
- Microphone and camera permissions require visible active state, explicit
  start/stop controls, paired-session preconditions, and disconnected-state
  shutdown behavior.
- `INTERNET` requires a transport interface review and must not include real
  host URLs or development endpoints.
- `VIBRATE` is acceptable only when tied to host feedback events or synthetic
  local preview state that is clearly labeled.
- Background capture, foreground services, storage permissions, and media file
  writes are out of scope for the public scaffold.

## Public/Private Leakage Grep Patterns

Use these scans on public integration paths. They are intentionally noisy and
must be reviewed with allowlists for policy text, schema field names, Android
namespace URLs, and synthetic fixture IDs.

```bash
rg -n --hidden -S \
  "(password|passwd|secret|token|credential|api[_-]?key|private[_-]?key|client[_-]?secret|bearer|authorization)" \
  README.md AGENTS.md docs examples prompts schemas scripts app build.gradle.kts settings.gradle.kts gradle.properties
```

```bash
rg -n --hidden -S \
  "(https?://|wss?://|localhost|127\\.0\\.0\\.1|0\\.0\\.0\\.0|192\\.168\\.|10\\.|172\\.(1[6-9]|2[0-9]|3[0-1])\\.|\\.local\\b|\\.lan\\b|\\.ts\\.net\\b|tailnet|tailscale)" \
  README.md AGENTS.md docs examples prompts schemas scripts app build.gradle.kts settings.gradle.kts gradle.properties
```

```bash
rg -n --hidden -S \
  "(device[_-]?id|host[_-]?id|pairing[_-]?(record|secret|token|payload)|session_[A-Za-z0-9_-]+|device_[A-Za-z0-9_-]+|host_[A-Za-z0-9_-]+)" \
  README.md AGENTS.md docs examples prompts schemas scripts app build.gradle.kts settings.gradle.kts gradle.properties
```

```bash
rg -n --hidden -S \
  "(transcript|screenshot|screen[-_ ]?record|recording|audio/[A-Za-z0-9.+-]+|video/[A-Za-z0-9.+-]+|image/[A-Za-z0-9.+-]+|\\.wav\\b|\\.mp3\\b|\\.m4a\\b|\\.mp4\\b|\\.webm\\b|\\.png\\b|\\.jpg\\b|\\.jpeg\\b)" \
  README.md AGENTS.md docs examples prompts schemas scripts app build.gradle.kts settings.gradle.kts gradle.properties
```

```bash
rg -n --hidden -S \
  "(-----BEGIN [A-Z ]*PRIVATE KEY-----|AKIA[0-9A-Z]{16}|AIza[0-9A-Za-z_-]{35}|xox[baprs]-[A-Za-z0-9-]+|sk-[A-Za-z0-9_-]+)" \
  README.md AGENTS.md docs examples prompts schemas scripts app build.gradle.kts settings.gradle.kts gradle.properties
```

Expected allowlisted hits:

- JSON Schema and Android namespace URLs.
- Public policy text naming forbidden categories.
- Synthetic fixture IDs such as `session_synthetic_demo`.
- Verification commands that contain the grep patterns themselves.

Disallowed hits:

- Any real host URL, private network address, pairing secret, credential,
  device identifier, live session ID, live transcript, screenshot, media file,
  operational log, or deployment configuration.

## Proposed Files

Recommended integration additions:

```text
docs/security-public-safety-review.md
prompts/public-safety-scan.prompt.md
scripts/scan_public_safety.sh
```

Optional later additions after Android files exist:

```text
app/src/test/java/org/lumen/lightcompanion/security/PermissionContractTest.kt
app/src/test/java/org/lumen/lightcompanion/security/PublicSafetyFixtureTest.kt
```

File intent:

- `docs/security-public-safety-review.md`: canonical public safety and
  permission review checklist derived from this packet.
- `prompts/public-safety-scan.prompt.md`: paired prompt for the scan script, in
  line with `AGENTS.md`.
- `scripts/scan_public_safety.sh`: repeatable grep scan over public paths.
- Permission tests: assert the scaffold manifest does not request microphone,
  camera, storage, background, notification, or network permissions until a
  reviewed packet intentionally adds them.
- Fixture tests: reject non-synthetic IDs, media filenames, encoded media,
  transcript text, URLs, and private network markers in public fixtures.

## Acceptance Criteria

- Public scaffold files contain no real private hosts, credentials, device IDs,
  pairing records, logs, transcripts, screenshots, media files, or deployment
  config.
- The Android manifest requests no dangerous permissions in the initial
  scaffold.
- Any future microphone or camera work is gated by explicit paired-session
  state, visible active state, user start/stop action, and disconnected shutdown.
- Event models preserve host ownership of canonical surface, transcript,
  artifact, and memory state.
- Synthetic examples remain clearly synthetic and deployment-neutral.
- Public-safety scans are documented, repeatable, and paired with a prompt file.
- The permissive event `payload` shape is tracked as a security review concern
  until payload schemas or typed Kotlin models narrow media and private-data
  fields.

## Verification Commands

Run schema validation:

```bash
python3 scripts/validate_companion_event.py examples/companion-event.example.json
```

Run public-safety scans:

```bash
rg -n --hidden -S "(password|passwd|secret|token|credential|api[_-]?key|private[_-]?key|client[_-]?secret|bearer|authorization)" README.md AGENTS.md docs examples prompts schemas scripts app build.gradle.kts settings.gradle.kts gradle.properties
rg -n --hidden -S "(https?://|wss?://|localhost|127\\.0\\.0\\.1|0\\.0\\.0\\.0|192\\.168\\.|10\\.|172\\.(1[6-9]|2[0-9]|3[0-1])\\.|\\.local\\b|\\.lan\\b|\\.ts\\.net\\b|tailnet|tailscale)" README.md AGENTS.md docs examples prompts schemas scripts app build.gradle.kts settings.gradle.kts gradle.properties
rg -n --hidden -S "(device[_-]?id|host[_-]?id|pairing[_-]?(record|secret|token|payload)|session_[A-Za-z0-9_-]+|device_[A-Za-z0-9_-]+|host_[A-Za-z0-9_-]+)" README.md AGENTS.md docs examples prompts schemas scripts app build.gradle.kts settings.gradle.kts gradle.properties
rg -n --hidden -S "(transcript|screenshot|screen[-_ ]?record|recording|audio/[A-Za-z0-9.+-]+|video/[A-Za-z0-9.+-]+|image/[A-Za-z0-9.+-]+|\\.wav\\b|\\.mp3\\b|\\.m4a\\b|\\.mp4\\b|\\.webm\\b|\\.png\\b|\\.jpg\\b|\\.jpeg\\b)" README.md AGENTS.md docs examples prompts schemas scripts app build.gradle.kts settings.gradle.kts gradle.properties
rg -n --hidden -S "(-----BEGIN [A-Z ]*PRIVATE KEY-----|AKIA[0-9A-Z]{16}|AIza[0-9A-Za-z_-]{35}|xox[baprs]-[A-Za-z0-9-]+|sk-[A-Za-z0-9_-]+)" README.md AGENTS.md docs examples prompts schemas scripts app build.gradle.kts settings.gradle.kts gradle.properties
```

Check Android permissions after scaffold files exist:

```bash
rg -n "uses-permission|RECORD_AUDIO|CAMERA|INTERNET|VIBRATE|POST_NOTIFICATIONS|FOREGROUND_SERVICE|READ_MEDIA_|WRITE_EXTERNAL_STORAGE|READ_EXTERNAL_STORAGE" app/src/main/AndroidManifest.xml app/src/main
```

Attempt Android build when the local toolchain is available:

```bash
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

## Dependencies

- `ripgrep` for public-safety scans.
- Python dependencies already used by `scripts/validate_companion_event.py`.
- Android SDK/JDK only after Android scaffold files are integrated.
- Optional shell script should use POSIX shell or Bash already present on local
  development machines.

No security scanner dependency is required for this first packet. If the repo
later adds CI, consider adding a dedicated secret scanner, but keep the initial
public-safety gate small and inspectable.

## Risks And Open Questions

- The current JSON Schema allows arbitrary `payload` properties. That is useful
  for early protocol sketching but risky for media, transcript, URL, filename,
  and private deployment leakage.
- `session_id` is required by the event schema, but the public fixture uses a
  synthetic session. Scans must distinguish field names and synthetic IDs from
  live session records.
- The roadmap includes microphone and camera capabilities, but retention,
  transport, and consent UX are not yet specified.
- The safest pairing model remains open: QR code, short code, authenticated
  registry, or another host-approved flow.
- The first scaffold may need `INTERNET` for future transport, but adding it too
  early weakens the no-live-network boundary.
- Local notes or generated files outside the listed public paths may trigger
  scans. The integration scan should target public repo paths and separately
  ensure local/private notes are not staged.
- A later CI workflow should fail on disallowed leakage hits, but policy-text
  allowlisting needs care to avoid noisy false positives.

## Current Review Notes

- The listed source docs preserve the correct product boundary: companion emits
  events and receives feedback; the hosted Lumen surface owns canonical state.
- The source docs consistently prohibit private hosts, credentials, device IDs,
  logs, transcripts, screenshots, and deployment configuration in this public
  repo.
- A local scan of the current workspace produced expected hits in policy text,
  schema URLs, synthetic fixture IDs, and draft verification commands. It also
  found local untracked notes outside the source list, so integration should
  review staged files explicitly before committing.
