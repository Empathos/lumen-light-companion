# Validate Companion Event Prompt

## Intent

Validate that a synthetic Lumen Light Companion event fixture matches the public
companion event schema.

## Inputs

- One JSON fixture path.
- `schemas/companion-event.schema.json`.

## Preconditions

- The fixture is synthetic and public-safe.
- The fixture does not include private device IDs, hostnames, credentials, live
  session IDs, transcripts, audio, images, or camera frames.

## Command

```bash
python3 -m venv .venv
. .venv/bin/activate
python3 -m pip install -r requirements.txt
python3 scripts/validate_companion_event.py examples/companion-event.example.json
```

## Expected Output

```text
LUMEN_LIGHT_COMPANION_EVENT_OK
```

## What Not To Do

- Do not validate private operational logs in this public repo.
- Do not commit real pairing records or live device identifiers.
- Do not add media payloads to public fixtures.
