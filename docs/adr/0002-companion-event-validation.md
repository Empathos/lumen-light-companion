# ADR 0002: Companion Event Validation

## Status

Accepted.

## Decision

Public fixtures are validated with the JSON schema validator and scaffold tests.
Kotlin code uses `kotlinx.serialization` for typed local event handling.

## Rationale

The schema is intentionally broad while the host protocol is still stabilizing.
Typed scaffold models and fixtures give contributors a stable contract without
hardcoding a live transport.

## Consequences

Payload-specific validation should tighten over time as touch, ink, feedback,
audio-state, and gesture command contracts stabilize.
