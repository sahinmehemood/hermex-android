# Optimus Project State

## Current status

Phase: native runtime integration + build stabilization.

### Completed

- GitHub write access verified.
- `optimus-android` long-lived development branch created.
- Product, architecture, design, upstream-parity and release documentation established.
- Premium native Android shell and semantic design system established.
- Legacy data-bound feature screens isolated/removed from the active build path so the native runtime can be validated independently.
- Native OkHttp WebSocket JSON-RPC transport added.
- Secure connection settings added with Android Keystore-backed token storage.
- Native Hermes session creation/resume gateway added.
- `AgentGateway` wired to the real Hermes `prompt.submit` protocol.
- Streaming event mapping added for message, reasoning, tool, approval, clarification, completion and error events.
- `session.interrupt` wired for native stop.
- Approval and clarification response flows wired to upstream JSON-RPC methods.
- Native connection onboarding validates gateway reachability before accepting the configuration.
- Live chat UI is now backed by the native gateway/session layer instead of a skin-only mock.
- Native long-running notification foundation and release CI remain in place.

## Current engineering gate

The next gate is a clean Android CI build on the latest Optimus commit. Only after that is green should additional feature slices be layered on top.

## Next engineering slices

1. Verify/fix the complete compile and lint surface.
2. Add Room-backed session/message persistence and reconnect recovery.
3. Replace the current minimal session lifecycle with production list/search/archive/pin/branch semantics.
4. Add attachment/media pipeline and Android file picker/share integration.
5. Rebuild workspace + Git operations with confirmation and rollback-safe behavior.
6. Rebuild skills, memory, tasks/cron, models/providers, profiles, and insights on the native repositories.
7. Add per-session prompt parking for approval/sudo/secret/clarify events and background-session notifications.
8. Add background agent lifecycle, resumable connections, exponential backoff and offline UX.
9. Add voice, deep links, shortcuts, widgets, accessibility and large-screen layouts.
10. Add contract tests against representative Hermes gateway frames and Compose critical-path tests.
11. Run full CI, harden release signing, and publish the first tested APK/AAB.

## Code-size policy

Do not manufacture lines solely to hit a target. The codebase should become large because the product contains real functionality, tests, platform integration, adapters, documentation and maintainable abstractions.

## Operational memory

At the start of each continuation, read this file, `PROJECT_SPEC.md`, `docs/ARCHITECTURE.md`, `docs/DESIGN_SYSTEM.md`, `docs/HERMES_PARITY.md`, and `docs/UPSTREAM.md`. Update this file after each major slice.
