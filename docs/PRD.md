# Product Requirements Document

## Product

Lumen Light Companion

## Summary

Lumen Light Companion is an Android-first companion app for interacting with a
hosted Lumen Light whiteboard. It provides touch, voice, haptic, and optional
camera/gesture input while the hosted Lumen session remains the source of truth
for surface state, transcript state, artifacts, and memory output.

## Problem

Hosted collaborative whiteboards are visible and durable, but direct interaction
can be awkward when users are not at the keyboard or when the host display is
being shared with others.

Phones and tablets are excellent personal input devices. They have touch,
microphones, cameras, motion sensors, and haptic feedback, but turning them into
separate whiteboards fragments the workspace.

The product problem is to make a hosted thinking surface physically interactive
without duplicating or forking the canonical whiteboard state.

## Goals

- Use an Android phone or tablet as a remote interaction surface for a hosted
  Lumen Light session.
- Support touch manipulation of hosted whiteboard objects.
- Support finger drawing that appears on the hosted surface.
- Capture close-range phone microphone audio into the hosted transcript pipeline.
- Provide haptic feedback for meaningful host/session state changes.
- Provide a path for camera and webcam gesture control.
- Preserve the host as the canonical owner of session state.
- Keep pairing, permissions, and event flow explicit and inspectable.

## Non-Goals

- Building an independent mobile whiteboard.
- Storing canonical Lumen session state on the phone.
- Shipping private deployment configuration in the public repo.
- Sending raw camera or microphone data without explicit user action.
- Replacing the hosted Lumen web surface.

## Primary Users

- A presenter or facilitator controlling a visible hosted surface.
- A participant using a phone as a near-field controller.
- A reviewer approving or rejecting staged agent-generated whiteboard changes.
- A user who wants close-range microphone capture while the surface is hosted
  elsewhere.

## Core Use Cases

### Touch Remote

The user pairs the companion with a hosted Lumen session and uses touch gestures
to select, move, resize, pan, zoom, and inspect objects on the host surface.

### Finger Ink

The user draws on the phone or tablet. The companion sends ink strokes to the
host as input events. The host renders and stores resulting surface changes.

### Haptic Feedback

The companion vibrates for state changes such as selected object, snap alignment,
accepted staged change, rejected staged change, boundary/collision, recording
start, recording pause, and pairing loss.

### Phone Microphone

The companion captures microphone audio and streams or batches it to the hosted
session transcript pipeline. The host turns audio into turns, normalized items,
surface changes, and artifacts.

### Air Drawing And Gesture Control

The companion or host camera observes hand movement and converts recognized
gestures into explicit surface commands such as draw stroke, select object, move
object, erase, zoom, or approve staged change.

## Functional Requirements

- Pair with a hosted Lumen session using a user-visible pairing flow.
- Maintain session presence and connection status.
- Emit touch events with normalized coordinates and target surface identifiers.
- Emit ink stroke events with stable stroke identifiers.
- Emit audio capture events linked to the active hosted session.
- Receive host feedback events and map them to haptic patterns.
- Receive host status events for recording, staging, object selection, and
  connection health.
- Support a safe disconnected state that stops media capture.
- Represent camera/gesture commands as explicit derived events.

## Privacy And Safety Requirements

- Do not persist raw microphone or camera content by default.
- Make active microphone and camera state visible to the user.
- Do not send media or sensor data without an active paired session.
- Keep private device IDs, network addresses, credentials, and live session logs
  out of the public repository.
- Treat camera gesture recognition as a derived-command system whenever possible.

## Event Boundary

The companion sends input events. The host sends feedback events.

Companion-to-host examples:

- `touch.select`
- `touch.move`
- `touch.resize`
- `ink.stroke`
- `audio.chunk`
- `gesture.air_draw`
- `gesture.approve`
- `gesture.reject`

Host-to-companion examples:

- `feedback.haptic`
- `surface.selection_changed`
- `surface.staged_change_ready`
- `surface.change_accepted`
- `surface.change_rejected`
- `session.recording_started`
- `session.recording_stopped`
- `session.connection_changed`

## Success Criteria

- A user can pair a companion device with a hosted Lumen session.
- Touch interactions update the hosted surface without local canonical state.
- Haptic responses reflect real host state changes.
- Phone microphone input reaches the hosted transcript pipeline.
- The public repo defines protocol boundaries without private operational data.
- Future gesture input can be added without changing the host ownership model.

## Open Questions

- Should first implementation be native Android, Kotlin Multiplatform, React
  Native, or a progressive web app wrapper?
- Should audio be streamed directly to the host or routed through a dedicated
  local media relay?
- Which haptic patterns are meaningful enough for a first release?
- Should gesture recognition run on-device, on the host, or both?
- What pairing model is safest for local-first use: QR code, short code, or
  authenticated device registry?
