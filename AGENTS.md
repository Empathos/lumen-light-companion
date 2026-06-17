# AGENTS.md

## Repository Role

This repository is the public scaffold for Lumen Light Companion.

Keep it generic and public-safe. Do not commit private device identifiers,
network hostnames, session IDs, credentials, transcripts, screenshots, or
deployment-specific configuration.

## Product Boundary

Lumen Light Companion is not the canonical whiteboard. It is a companion input
and feedback surface for a hosted Lumen Light session.

The hosted Lumen surface owns session state. The companion emits paired input
events and receives feedback events.

## Public/Private Split

Public repo:

- Product requirements
- Architecture
- Protocol sketches
- Synthetic examples
- Public-safe validation scaffolding

Private downstream repo or branch:

- Real device pairing
- Local network configuration
- Deployment credentials
- Operational logs
- Live session recordings

## Development Notes

- Prefer small, inspectable contracts before native app implementation.
- Keep Android implementation decisions behind the product protocol until the
  host-side Lumen surface protocol stabilizes.
- Every operational script should have a paired prompt file.
