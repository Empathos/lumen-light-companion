# Development

This repository is the public scaffold for Lumen Light Companion. Keep local
devices, host routes, credentials, logs, transcripts, screenshots, and deployment
configuration outside this repo.

## Public Checks

```bash
python3 -m pip install -r requirements.txt
python3 scripts/validate_companion_event.py examples/companion-event.example.json
python3 scripts/public_safe_check.py
```

## Android Checks

The Android scaffold requires a local JDK, Android SDK, and Gradle wrapper or
compatible Gradle installation.

```bash
./gradlew :app:testDebugUnitTest
./gradlew :app:assembleDebug
```

The current scaffold has no live networking, no real pairing, no raw microphone
or camera capture, and no deployment configuration.
