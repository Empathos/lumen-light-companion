# Agent 9 Draft Packet: Tests, CI, And Developer Docs

## Scope

Define the public-safe test, validation, CI, and developer-documentation runway
for the Android scaffold. This packet does not add production Android behavior,
real pairing, transport, microphone/camera capture, deployment configuration, or
private environment details.

The goal is to give the integrator a small set of repeatable checks that prove:

- public JSON fixtures match the companion event schema;
- Kotlin event/model code has fast JVM tests once the Android scaffold lands;
- CI stays generic and safe for a public repository;
- README and ADR documentation explain how contributors verify the scaffold.

## Proposed Files

```text
.github/workflows/ci.yml
docs/adr/0001-public-safe-android-scaffold.md
docs/adr/0002-companion-event-validation.md
docs/DEVELOPMENT.md
prompts/ci-public-safe-check.prompt.md
scripts/public_safe_check.py
app/src/test/java/org/lumen/lightcompanion/events/CompanionEventContractTest.kt
app/src/test/java/org/lumen/lightcompanion/pairing/PairingStateTest.kt
app/src/test/java/org/lumen/lightcompanion/feedback/HapticFeedbackMappingTest.kt
app/src/test/java/org/lumen/lightcompanion/transport/CompanionTransportContractTest.kt
app/src/test/resources/fixtures/README.md
```

Optional, if the integrator prefers one command for local verification:

```text
scripts/verify_public_scaffold.sh
prompts/verify-public-scaffold.prompt.md
```

Every operational script should have a paired prompt file, matching `AGENTS.md`.

## Unit Test Plan

### Event Contract Tests

Owns fast JVM coverage for model/schema alignment after Agents 3 and 4 land.

Test cases:

- decode the synthetic touch, ink, and haptic fixtures;
- assert required top-level fields are present;
- assert `device_role` matches event direction where known:
  `companion` for input events, `host` for feedback/status events;
- assert normalized coordinate fields remain within `0.0..1.0`;
- assert sequence numbers are non-negative;
- assert round-trip JSON semantics if `kotlinx.serialization` is accepted.

### Pairing State Tests

Owns reducer/state-machine behavior from Agent 5.

Test cases:

- initial state is disconnected/unpaired;
- manual or QR placeholder input moves only into a synthetic pending state;
- pairing loss returns to a safe disconnected state;
- media/capture flags are false when disconnected;
- no test fixture contains real hostnames, IPs, device IDs, or pairing records.

### Feedback And Haptic Mapping Tests

Owns host feedback to local feedback intent mapping. These are JVM tests, not
device vibration tests.

Test cases:

- `feedback.haptic` maps to a named scaffold pattern;
- selection, staged-change, accepted, rejected, recording, and disconnected
  feedback produce deterministic public-safe pattern names;
- unknown feedback falls back to no-op or generic status without inventing host
  state.

### Transport Contract Tests

Owns interface boundaries from Agent 6 without live network behavior.

Test cases:

- fake transport records outbound companion events;
- fake transport can inject host feedback events;
- disconnected transport rejects or queues sends according to the accepted
  transport interface contract;
- tests never open sockets, call real URLs, or depend on local network state.

## Validation Scripts

### Existing Schema Validator

Keep and use:

```bash
python3 scripts/validate_companion_event.py examples/companion-event.example.json
```

Expected output:

```text
LUMEN_LIGHT_COMPANION_EVENT_OK
```

Recommended extension:

- allow multiple fixture paths in one invocation, or keep the single-file script
  and have CI call it once per fixture;
- keep failure output concise and path-specific;
- do not validate private logs or live recordings in this public repo.

### Public-Safety Check

Add a conservative scanner for accidental public/private boundary leaks. It
should scan proposed public scaffold paths and fail on obvious credential,
network, transcript, media, and deployment markers.

Suggested path scope:

```text
README.md
AGENTS.md
docs
examples
prompts
schemas
scripts
app/src/main
app/src/test
.github/workflows
```

Suggested blocked patterns:

```text
password
secret
token
credential
private_key
api_key
device_[A-Za-z0-9_-]+
pairing_[A-Za-z0-9_-]+
transcript
screenshot
audio/
video/
http://
https://
[0-9]+\.[0-9]+\.[0-9]+\.[0-9]+
```

Allowlist synthetic schema/example IDs such as `session_synthetic_demo`,
`surface_synthetic_hosted_whiteboard`, and `event_synthetic_*`.

Also allowlist public infrastructure references required by the scaffold:

- JSON Schema metadata URLs in `schemas/*.json`;
- GitHub Actions marketplace references such as `actions/checkout@v4`;
- generic package repositories and dependency coordinates if they are later
  checked into Gradle files.

Rationale: this check is intentionally blunt. It is a CI tripwire, not a
security scanner.

## CI Skeleton

Proposed `.github/workflows/ci.yml`:

```yaml
name: CI

on:
  pull_request:
  push:
    branches: [main]

permissions:
  contents: read

jobs:
  public-contract:
    name: Public contract checks
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up Python
        uses: actions/setup-python@v5
        with:
          python-version: "3.12"

      - name: Install Python dependencies
        run: python -m pip install -r requirements.txt

      - name: Validate public companion event fixture
        run: python scripts/validate_companion_event.py examples/companion-event.example.json

      - name: Public-safety scan
        run: python scripts/public_safe_check.py

  android:
    name: Android scaffold checks
    runs-on: ubuntu-latest
    if: ${{ hashFiles('settings.gradle.kts') != '' }}
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "17"
          cache: gradle

      - name: Set up Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Run JVM unit tests
        run: ./gradlew :app:testDebugUnitTest

      - name: Assemble debug scaffold
        run: ./gradlew :app:assembleDebug
```

Notes:

- No signing, release, artifact upload, deployment, emulator, or private
  configuration.
- The Android job is conditional so the current pre-Android repo can still run
  public contract checks.
- Add `./gradlew lint` later only after the scaffold and lint baseline are
  intentionally defined.

## README And Developer Docs Updates

### `README.md`

Add a "Development checks" section after the existing validation command:

```bash
python3 -m pip install -r requirements.txt
python3 scripts/validate_companion_event.py examples/companion-event.example.json
python3 scripts/public_safe_check.py
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

Clarify that Gradle commands require the Android scaffold and local Android SDK.

### `docs/DEVELOPMENT.md`

Recommended content:

- prerequisites: Python 3.12+, JDK 17, Android SDK when Android files exist;
- local setup commands;
- public/private safety rules for fixtures, docs, and tests;
- how to add a fixture and validate it;
- how to run unit tests;
- how to interpret CI failures;
- rule that fake transports and synthetic fixtures are preferred until the host
  protocol stabilizes.

### ADRs

`docs/adr/0001-public-safe-android-scaffold.md`:

- decision: keep Android scaffold generic, native Kotlin/Compose, no private
  deployment settings;
- consequence: verification may be conditional until Android toolchain exists.

`docs/adr/0002-companion-event-validation.md`:

- decision: validate public fixtures with JSON Schema plus Kotlin JVM tests;
- consequence: schema proves top-level contract while JVM tests prove
  scaffold-specific mappings and reducers.

## Acceptance Criteria

- CI validates the existing public fixture against
  `schemas/companion-event.schema.json`.
- CI includes a public-safety scan for obvious private data and deployment
  markers.
- Android CI runs only after Gradle scaffold files exist.
- JVM tests cover event contracts, pairing safe-state behavior, haptic mapping,
  and fake transport boundaries.
- Tests use synthetic fixtures only and include no real hostnames, IPs,
  credentials, device IDs, pairing records, logs, transcripts, screenshots, or
  media payloads.
- README and developer docs explain local verification without requiring private
  infrastructure.
- ADRs capture why validation is schema-first and why CI remains deployment-free.

## Verification Commands

Current repo checks:

```bash
python3 -m pip install -r requirements.txt
python3 scripts/validate_companion_event.py examples/companion-event.example.json
rg -n "password|secret|token|credential|private_key|api_key|device_[A-Za-z0-9_-]+|pairing_[A-Za-z0-9_-]+|transcript|screenshot|audio/|video/|http://|https://|[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+" README.md AGENTS.md docs examples prompts schemas scripts
```

Until `scripts/public_safe_check.py` exists, treat the `rg` command as a manual
triage aid. It is expected to report some legitimate docs/schema references that
the final script should allowlist.

After Android scaffold lands:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

After public-safety script lands:

```bash
python3 scripts/public_safe_check.py
```

Expected schema validator output:

```text
LUMEN_LIGHT_COMPANION_EVENT_OK
```

## Dependencies

- Python `jsonschema` from `requirements.txt`.
- GitHub Actions:
  - `actions/checkout@v4`
  - `actions/setup-python@v5`
  - `actions/setup-java@v4`
  - `gradle/actions/setup-gradle@v4`
- JDK 17 for Android Gradle Plugin 8.x.
- Android SDK matching the scaffold `compileSdk`.
- Kotlin/JUnit/serialization dependencies accepted by Agents 1, 3, and 4.

## Risks And Open Questions

- The current schema allows arbitrary payload fields, so Kotlin tests must carry
  shape-specific proof until the schema becomes stricter.
- A blunt public-safety scan can produce false positives in docs that discuss
  forbidden terms. The script should support narrow allowlist comments or path
  exclusions, but the default should fail closed.
- Android CI may be slow on first runs because Gradle and SDK caches are cold.
- `assembleDebug` verifies buildability but not UI behavior; Compose UI tests can
  be added after Agent 2's shell stabilizes.
- CI should not run emulator tests in the first scaffold unless a later packet
  explicitly adds them.
- The integrator should decide whether fixture validation remains one-file-at-a
  time or expands to a directory/multi-file validator.
