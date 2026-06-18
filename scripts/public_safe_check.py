#!/usr/bin/env python3
"""Small public-safety tripwire for the scaffold repository."""

from __future__ import annotations

import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SCOPES = [
    "README.md",
    "AGENTS.md",
    "docs",
    "examples",
    "prompts",
    "schemas",
    "scripts",
    "app/src/main",
    "app/src/test",
    ".github/workflows",
    "build.gradle.kts",
    "settings.gradle.kts",
    "gradle.properties",
]

BLOCKED = [
    re.compile(pattern, re.IGNORECASE)
    for pattern in [
        r"\b(password|passwd|credential|private_key|api_key|client_secret|bearer|authorization)\b\s*[:=]",
        r"(localhost|127\.0\.0\.1|0\.0\.0\.0|192\.168\.\d{1,3}\.\d{1,3}|10\.\d{1,3}\.\d{1,3}\.\d{1,3}|172\.(1[6-9]|2[0-9]|3[0-1])\.\d{1,3}\.\d{1,3}|\.local\b|\.lan\b|\.ts\.net\b|tailnet|tailscale)",
        r"(device_id|host_id|pairing_(secret|token|payload|record))",
        r"(screen[-_ ]?record|audio/[A-Za-z0-9.+-]+|video/[A-Za-z0-9.+-]+|image/[A-Za-z0-9.+-]+|\.(wav|mp3|m4a|mp4|webm|png|jpg|jpeg)\b)",
        r"(-----BEGIN [A-Z ]*PRIVATE KEY-----|AKIA[0-9A-Z]{16}|AIza[0-9A-Za-z_-]{35}|xox[baprs]-[A-Za-z0-9-]+|sk-[A-Za-z0-9_-]{20,})",
    ]
]

ALLOW_PATTERNS = [
    "https://json-schema.org/",
    "https://example.org/",
    "actions/checkout",
    "actions/setup-python",
    "actions/setup-java",
    "gradle/actions",
    "session_synthetic_demo",
    "surface_synthetic_hosted_whiteboard",
]


def iter_files() -> list[Path]:
    files: list[Path] = []
    for scope in SCOPES:
        path = ROOT / scope
        if path.is_file():
            files.append(path)
        elif path.is_dir():
            files.extend(p for p in path.rglob("*") if p.is_file())
    files = [
        path
        for path in files
        if path.relative_to(ROOT).as_posix() not in {
            "docs/AGENT_PACKET_PLAN.md",
            "scripts/public_safe_check.py",
        }
        and "docs/agent-drafts" not in path.relative_to(ROOT).as_posix()
    ]
    return sorted(set(files))


def allowed(line: str) -> bool:
    return any(marker in line for marker in ALLOW_PATTERNS)


def main() -> int:
    findings: list[str] = []
    for path in iter_files():
        rel = path.relative_to(ROOT)
        text = path.read_text(encoding="utf-8", errors="ignore")
        for number, line in enumerate(text.splitlines(), start=1):
            if allowed(line):
                continue
            if any(pattern.search(line) for pattern in BLOCKED):
                findings.append(f"{rel}:{number}: {line.strip()}")

    if findings:
        print("PUBLIC_SAFE_CHECK_FAILED")
        print("\n".join(findings[:200]))
        return 1

    print("PUBLIC_SAFE_CHECK_OK")
    return 0


if __name__ == "__main__":
    sys.exit(main())
