# Lumen Light Companion

Lumen Light Companion is a touch-native control surface for a hosted Lumen Light
whiteboard.

It is not a phone-sized whiteboard. The hosted surface remains the canonical
workspace. The companion turns a phone or tablet into a nearby interaction layer:
touch control, finger ink, haptic feedback, microphone capture, and eventually
camera-based gesture input for drawing or manipulating objects in the air.

The goal is to make live conversation surfaces easier to steer while preserving a
single durable source of truth in the hosted Lumen session.

## Why it exists

Large shared thinking surfaces are useful when they remain visible: a monitor,
projector, desktop browser, wall display, or collaboration host. Phones are
useful because they are personal, touch-native, sensor-rich, and physically close
to the user.

Lumen Light Companion connects those strengths. The host shows the workspace.
The companion provides direct manipulation, close-range audio, haptic state
feedback, and optional gesture sensing without splitting the whiteboard into a
separate mobile copy.

## Core idea

```text
touch / mic / camera / sensors
        |
        v
Lumen Light Companion
        |
        v
paired input + media events
        |
        v
hosted Lumen Light session
        |
        v
surface updates + feedback events
        |
        v
haptics / status / controls
```

The companion emits commands. The host owns state.

## What Lumen Light Companion manages

- Touch selection, drag, resize, pan, zoom, and approve/reject controls.
- Finger ink that appears on the hosted whiteboard.
- Haptic responses for selection, snapping, staged changes, recording state, and
  accepted or rejected updates.
- Phone microphone capture for the hosted session transcript and turn queue.
- Camera or webcam gesture input for air drawing and hand-based commands.
- Pairing and session presence for a single hosted Lumen surface.
- Public-safe event contracts for companion-to-host and host-to-companion flow.

## Design principles

- The host owns canonical session state.
- The companion is an input and feedback layer, not a competing workspace.
- Touch, voice, haptics, and gesture events should share one event contract.
- Raw media should be minimized; derived commands and explicit consent matter.
- Haptic feedback should communicate state, not distract.
- Companion features should degrade gracefully when sensors are unavailable.
- Public examples must stay synthetic and deployment-neutral.

## Repository layout

```text
.
├── AGENTS.md
├── README.md
├── docs/
│   ├── ARCHITECTURE.md
│   ├── DEVELOPMENT.md
│   ├── PRD.md
│   ├── ROADMAP.md
│   ├── adr/
│   └── security-public-safety-review.md
├── app/
│   └── src/
├── examples/
│   └── companion-event.example.json
├── prompts/
│   └── validate-companion-event.prompt.md
├── schemas/
│   └── companion-event.schema.json
├── scripts/
│   └── validate_companion_event.py
├── requirements.txt
```

## Current status

This repository is an early Android product scaffold. It captures the companion
concept, initial product requirements, protocol boundaries, synthetic event
examples, and a thin native Android/Compose app runway.

The Android scaffold is fake/local only. It has no live pairing, no live
transport, no raw microphone or camera capture, no dangerous Android
permissions, and no deployment configuration.

Run the public-safe validation check:

```bash
python3 -m venv .venv
. .venv/bin/activate
python3 -m pip install -r requirements.txt
python3 scripts/validate_companion_event.py examples/companion-event.example.json
python3 scripts/public_safe_check.py
```

Expected output:

```text
LUMEN_LIGHT_COMPANION_EVENT_OK
```

Android verification requires a local JDK, Android SDK, and Gradle wrapper or
compatible Gradle installation:

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

## Public/private model

Use this repository as the generic upstream. Keep environment-specific
customizations in private downstream repositories or private branches.

```text
empathos/lumen-light-companion     public generic framework
private downstream fork            local devices, pairing, credentials, logs
```

This keeps the public framework reusable while preserving operational privacy.
