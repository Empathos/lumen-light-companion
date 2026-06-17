# Architecture

## Boundary

Lumen Light Companion is a client for a hosted Lumen Light session.

The companion does not own canonical whiteboard state. It owns local input
capture, local permission state, user feedback, and connection state.

## System Shape

```text
Android companion
  touch layer
  ink layer
  microphone layer
  haptic layer
  camera/gesture layer
        |
        v
companion event transport
        |
        v
hosted Lumen session
  pairing
  event router
  surface state
  transcript pipeline
  staged/live state
  artifact emitter
        |
        v
feedback event transport
        |
        v
Android companion
  haptics
  status
  controls
```

## Companion Responsibilities

- Pair with a host session.
- Capture touch, ink, audio, and optional camera/gesture input.
- Normalize local coordinates against a known host surface viewport.
- Send input events with session and surface context.
- Display connection, permission, recording, and selection state.
- Convert host feedback events into haptic patterns.
- Stop capture when disconnected or unpaired.

## Host Responsibilities

- Authenticate or approve pairing.
- Own canonical surface state.
- Resolve object identifiers and surface coordinates.
- Apply, reject, or stage companion events.
- Preserve event provenance for artifacts.
- Send feedback events to the companion.
- Own transcript, turn queue, memory enrichment, and durable artifacts.

## Event Transport

The first implementation should keep the transport simple. Candidate transports:

- WebSocket for bidirectional low-latency control events.
- WebRTC for realtime audio and potential camera streams.
- HTTP for pairing, session metadata, and low-frequency status.

The event contract should be transport-independent.

## Coordinate Model

Touch and ink events should include:

- `surface_id`
- `viewport_id`
- normalized coordinates
- optional object target
- client timestamp
- sequence number

The host maps normalized input onto canonical surface coordinates.

## Haptic Model

Haptics should be driven by host feedback events, not guessed locally.

Examples:

- light tick: selection changed
- double tick: snap alignment
- short buzz: staged change ready
- firm tick: change accepted
- soft reject: change rejected
- pulse: recording started
- fade/pattern: disconnected

Exact platform mappings belong in implementation docs once Android code exists.

## Media Model

Microphone and camera inputs are higher-risk than touch events.

Initial audio should support explicit start/stop state and visible status. Camera
gesture input should prefer derived gesture commands over raw video transport
when possible.

## Public/Private Boundary

Public architecture may describe pairing and event flow generically. Private
downstream implementations hold real host URLs, device IDs, credentials, pairing
records, logs, and media retention policy.
