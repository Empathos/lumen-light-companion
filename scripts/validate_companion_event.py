#!/usr/bin/env python3
"""Validate a Lumen Light Companion event fixture."""

import json
import sys
from pathlib import Path

from jsonschema import Draft202012Validator, FormatChecker


def main() -> int:
    if len(sys.argv) != 2:
        print("LUMEN_LIGHT_COMPANION_EVENT_INVALID:expected one fixture path")
        return 2

    repo_root = Path(__file__).resolve().parents[1]
    schema_path = repo_root / "schemas" / "companion-event.schema.json"
    fixture_path = Path(sys.argv[1])

    try:
        schema = json.loads(schema_path.read_text(encoding="utf-8"))
        fixture = json.loads(fixture_path.read_text(encoding="utf-8"))
    except Exception as exc:
        print(f"LUMEN_LIGHT_COMPANION_EVENT_INVALID:{exc}")
        return 1

    validator = Draft202012Validator(schema, format_checker=FormatChecker())
    errors = sorted(validator.iter_errors(fixture), key=lambda error: error.path)
    if errors:
        first = errors[0]
        location = ".".join(str(part) for part in first.path) or "<root>"
        print(f"LUMEN_LIGHT_COMPANION_EVENT_INVALID:{location}:{first.message}")
        return 1

    print("LUMEN_LIGHT_COMPANION_EVENT_OK")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
