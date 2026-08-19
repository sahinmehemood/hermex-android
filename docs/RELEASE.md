# Optimus Android Release Checklist

## GitHub Secrets

Production signing requires these repository secrets:

- `ANDROID_KEYSTORE_BASE64`
- `ANDROID_KEYSTORE_PASSWORD`
- `ANDROID_KEY_ALIAS`
- `ANDROID_KEY_PASSWORD`

The release workflow falls back to the debug signing key only for personal-test builds. Do not use that fallback for public distribution.

## Build path

Push a branch named `release/<version>` from `optimus-android`. GitHub Actions builds the release APK and AAB, then creates the corresponding `v<version>` GitHub Release.

## Pre-release gate

- Android CI green on the exact commit.
- Unit tests green.
- Compose/UI tests green for changed critical paths.
- Lint green.
- No credentials/secrets in tracked files.
- APK installs successfully on a physical Android device.
- Remote Hermes connection succeeds.
- Streaming chat succeeds.
- Tool activity renders.
- Approval flow succeeds.
- Session history resumes correctly.
- Offline cached history is readable.
