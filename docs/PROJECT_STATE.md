# Optimus Project State

## Current status

Phase: active native rearchitecture.

### Completed

- GitHub connector write access verified.
- Development branch `optimus-android` created.
- Draft PR #1 established as the long-lived integration point.
- Product specification and durable project memory added.
- Hermes Desktop parity research and mobile architecture notes added.
- CI workflow added for unit tests, lint, and debug APK builds.
- Release workflow added for APK/AAB GitHub Releases with repository-secret signing support.
- Gradle versioning/signing support added.
- Premium Optimus semantic color palette and typography added.
- Adaptive phone/tablet navigation shell rebuilt.
- Reusable status/tool/approval/run-control UI primitives added.
- Formal agent run state machine added with reducer tests.
- Stable domain-facing `AgentGateway` contract added.
- `AgentRunViewModel` added for run orchestration and cancellation.

## Architecture target

Native Kotlin + Jetpack Compose. Remote-first Hermes backend. UI, domain, transport, cache, security, and Android platform concerns remain separated.

## Immediate next slices

1. Wire the existing Hermes REST/WebSocket implementation into `AgentGateway`.
2. Rebuild the actual chat transcript/composer around `AgentRunState`.
3. Add encrypted credential/session persistence.
4. Add offline session/message cache and reconnect policy.
5. Add attachment pipeline for Android files/images/audio.
6. Rebuild workspace/file preview and Git safety flows.
7. Harden skills, memory, tasks, models, profiles, and insights screens against the new design system.
8. Add notifications, shortcuts, share targets, and deep links.
9. Expand UI/integration test coverage.
10. Run CI, resolve all build/lint/test issues, then publish the first usable APK release.

## Release rule

Do not call a release production-ready until CI produces an APK/AAB from the exact branch/tag being released and the GitHub Release contains the artifacts.

## Research basis

The official Hermes Desktop app is documented as a native Electron + React product using a headless `hermes serve` backend and JSON-RPC/WebSocket transport. Optimus intentionally does not embed that renderer in a WebView; it adopts the desktop product model and capabilities in a native Android implementation.
