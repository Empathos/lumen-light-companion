# Security And Public-Safety Review

The initial scaffold must not include private hosts, credentials, device IDs,
pairing records, live logs, transcripts, screenshots, media files, or deployment
configuration.

## Permission Boundary

The Android manifest requests no dangerous permissions in the initial scaffold.
Future microphone, camera, network, notification, storage, foreground-service,
or haptic-service work requires explicit review and visible user controls.

## Scaffold Controls

- Use synthetic IDs only.
- Keep host ownership explicit in docs, models, and UI copy.
- Model audio and camera as permission/status state before capture.
- Keep transport behind interfaces and fake local implementations.
- Do not buffer media or input while disconnected.

## Verification

```bash
python3 scripts/validate_companion_event.py examples/companion-event.example.json
python3 scripts/public_safe_check.py
rg -n "uses-permission|RECORD_AUDIO|CAMERA|INTERNET|VIBRATE|POST_NOTIFICATIONS|FOREGROUND_SERVICE|READ_MEDIA_|WRITE_EXTERNAL_STORAGE|READ_EXTERNAL_STORAGE" app/src/main/AndroidManifest.xml app/src/main
```
