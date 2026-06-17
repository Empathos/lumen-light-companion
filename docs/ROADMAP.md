# Roadmap

## Phase 0: Product Contract

- Capture PRD and architecture.
- Define companion/host boundary.
- Add synthetic event examples.
- Draft event schema.
- Decide first app stack only after host protocol is stable.

## Phase 1: Pairing And Touch Remote

- Pair a companion device to a hosted Lumen session.
- Send touch select/move/pan/zoom events.
- Receive selection and connection feedback.
- Map basic feedback to haptic patterns.

## Phase 2: Finger Ink

- Capture finger strokes on the companion.
- Send normalized ink events to the host.
- Render strokes on the hosted whiteboard.
- Add undo/cancel for pending local strokes.

## Phase 3: Phone Microphone

- Add explicit microphone start/stop.
- Route audio into the hosted transcript pipeline.
- Show visible recording state on companion and host.
- Add privacy and retention controls.

## Phase 4: Review Controls

- Approve or reject staged host changes.
- Navigate staged objects and transcript turns.
- Add haptic patterns for review outcomes.

## Phase 5: Camera And Gesture Input

- Prototype hand tracking with phone camera or host webcam.
- Convert gestures into explicit surface commands.
- Support air drawing, select, move, erase, approve, and reject gestures.
- Keep raw video handling optional and explicit.

## Phase 6: Public Implementation

- Add Android app skeleton.
- Add event transport client.
- Add public-safe tests and synthetic fixtures.
- Add local demo host configuration without private deployment values.
