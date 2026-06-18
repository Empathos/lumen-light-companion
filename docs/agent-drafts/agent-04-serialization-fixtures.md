# Agent 4 Draft Packet: Serialization And Fixtures

## Scope

Add JSON serialization support, public-safe synthetic event fixtures, and unit
tests proving Android/Kotlin event models can round-trip through the public
companion event shape. This packet assumes Agent 3 owns the final model types and
the integrator will align package paths with the accepted Android scaffold.

No live session examples, real user records, device identifiers, hostnames,
credentials, transcripts, audio, images, camera frames, screenshots, or
deployment configuration belong in this packet.

## JSON Serialization Choice

Use `kotlinx.serialization` with `kotlinx.serialization.json.Json`.

Rationale:

- It is Kotlin-first and works cleanly with Android/JVM unit tests.
- It avoids reflection-heavy runtime behavior.
- It supports sealed hierarchies, enums, value objects, and explicit field names.
- It can be configured to reject unknown fields for contract fixtures while still
  keeping future transport handling separate.

Recommended Gradle additions after Agent 1 creates Android build files:

```kotlin
plugins {
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21" apply false
}
```

```kotlin
plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
}
```

Recommended shared JSON configuration:

```kotlin
internal val CompanionJson = Json {
    explicitNulls = false
    ignoreUnknownKeys = false
    encodeDefaults = true
}
```

## Proposed Files

```text
app/src/main/java/org/lumen/lightcompanion/events/CompanionJson.kt
app/src/test/resources/fixtures/companion-event-touch-move.json
app/src/test/resources/fixtures/companion-event-ink-stroke.json
app/src/test/resources/fixtures/companion-event-feedback-haptic.json
app/src/test/java/org/lumen/lightcompanion/events/CompanionEventSerializationTest.kt
```

Optional, if the integrator wants non-Android validation coverage for the public
fixture examples:

```text
examples/companion-event.touch-move.example.json
examples/companion-event.ink-stroke.example.json
examples/companion-event.feedback-haptic.example.json
```

Keep `examples/companion-event.example.json` as the existing public smoke-test
fixture unless the integrator intentionally broadens the Python validator.

## Synthetic Fixtures

All fixture IDs must remain obviously synthetic:

- `session_synthetic_demo`
- `surface_synthetic_hosted_whiteboard`
- `viewport_phone_touch_surface`
- `object_synthetic_card_001`
- `stroke_synthetic_ink_001`
- `event_synthetic_*`

Fixture set:

- `touch.move`: mirrors the existing example and proves normalized pointer
  payload fields survive round-trip encoding.
- `ink.stroke`: uses synthetic stroke and point data with normalized
  coordinates; no image or raw drawing asset.
- `feedback.haptic`: host-origin feedback event with a named public-safe haptic
  pattern such as `selection_move_tick` or `change_accepted_tick`.

Avoid fixtures for `audio.chunk` in this packet unless the payload is only a
metadata state event. Do not include encoded audio, transcript text, filenames,
or media references.

## Round-Trip Tests

Unit tests should run on the JVM with `./gradlew :app:testDebugUnitTest`.

Test cases:

- Decode each fixture into the Agent 3 event model.
- Assert required top-level fields:
  `event_id`, `event_type`, `session_id`, `surface_id`, `device_role`,
  `timestamp`, `sequence`, and `payload`.
- Encode the model back to JSON.
- Decode the encoded JSON again and assert semantic equality with the first
  decoded model.
- Assert enum/string mapping for at least:
  `touch.move`, `ink.stroke`, and `feedback.haptic`.
- Assert normalized coordinate payloads stay within `0.0..1.0` for touch and
  ink fixture points.

If Agent 3 uses strongly typed payload classes, tests should also verify that an
event type maps to the expected payload type. If Agent 3 keeps payloads generic
for the first scaffold, tests should assert required payload keys instead.

## Acceptance Criteria

- Serialization uses `kotlinx.serialization`, not ad hoc string parsing.
- Fixtures validate against `schemas/companion-event.schema.json` where they use
  the existing public schema shape.
- Fixtures are synthetic, deployment-neutral, and contain no real user, device,
  network, session, media, transcript, screenshot, or credential data.
- Round-trip tests prove decode, encode, and decode-again behavior for touch,
  ink, and haptic feedback events.
- Tests preserve host ownership of canonical state: companion fixtures describe
  input events and host fixtures describe feedback/status events only.
- No transport, pairing, WebSocket, WebRTC, microphone capture, or camera capture
  implementation is introduced by this packet.

## Verification Commands

```bash
./gradlew :app:testDebugUnitTest
python3 scripts/validate_companion_event.py examples/companion-event.example.json
python3 scripts/validate_companion_event.py app/src/test/resources/fixtures/companion-event-touch-move.json
python3 scripts/validate_companion_event.py app/src/test/resources/fixtures/companion-event-ink-stroke.json
python3 scripts/validate_companion_event.py app/src/test/resources/fixtures/companion-event-feedback-haptic.json
rg -n "http://|https://|[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+|password|secret|token|credential|device_[A-Za-z0-9_-]+|transcript|screenshot|audio/[A-Za-z0-9.+-]+" app/src/test/resources examples app/src/test || true
```

Expected Python validator output for each valid fixture:

```text
LUMEN_LIGHT_COMPANION_EVENT_OK
```

## Dependencies

- Kotlin serialization compiler plugin aligned with the Kotlin version chosen by
  Agent 1.
- `org.jetbrains.kotlinx:kotlinx-serialization-json`.
- JUnit already proposed by Agent 1 for JVM unit tests.
- Existing Python JSON schema validator for public fixture smoke checks.

## Risks And Open Questions

- Agent 3 model decisions may determine whether payloads are strongly typed,
  polymorphic, or temporarily generic JSON objects.
- The current JSON schema allows arbitrary payload fields, so Kotlin tests must
  carry more shape-specific proof than the schema currently enforces.
- `ignoreUnknownKeys = false` is good for fixture discipline, but transport
  handling may later need a more tolerant decode path for forward compatibility.
- Fixture paths under `app/src/test/resources` depend on the Android scaffold
  accepted by the integrator.
- Adding multiple example files may require broadening the existing validation
  script or adding a paired prompt if the integrator creates a new operational
  script.
