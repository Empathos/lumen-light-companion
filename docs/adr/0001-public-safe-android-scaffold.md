# ADR 0001: Public-Safe Android Scaffold

## Status

Accepted.

## Decision

The first Android scaffold uses native Kotlin and Jetpack Compose with a
public-safe package name, fake local state, no dangerous permissions, no live
transport, and synthetic fixture data only.

## Rationale

The companion is an input and feedback surface for a hosted Lumen session. The
host owns canonical surface state, session state, transcript state, artifacts,
and durable memory. The public repository should provide a reviewable runway
without exposing private deployment details or implying a stable live protocol.

## Consequences

Real pairing, media capture, transport adapters, signing, and deployment remain
private downstream or future reviewed work.
