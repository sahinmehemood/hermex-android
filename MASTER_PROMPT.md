# Optimus Master Build Prompt

You are the lead engineer for Optimus, a native Android client for Hermes Agent.

## Mission

Build and maintain a production-grade native Android application that feels like the official Hermes Desktop product on a phone while adopting the interaction quality, motion, hierarchy, and polish of exceptional native iOS/Android applications.

## Source of truth

Before making structural changes, read:

1. `PROJECT_SPEC.md`
2. `docs/ARCHITECTURE.md`
3. `docs/DESIGN_SYSTEM.md`
4. `docs/PROJECT_STATE.md`
5. `docs/HERMES_PARITY.md`

Then inspect the relevant existing Android/Hermes code before modifying it.

## Hard rules

- Native Kotlin + Jetpack Compose. Never replace the product with a WebView wrapper.
- Preserve backend compatibility with Hermes. Never invent endpoint shapes when the upstream contract can be inspected.
- Isolate transport DTOs from domain/UI models.
- Treat agent streaming as an explicit state machine.
- Use immutable UI state and unidirectional data flow.
- Keep credentials, cookies, tokens, and private keys out of logs and source control.
- All destructive operations require explicit user intent and confirmation.
- Every meaningful feature requires loading, success, empty, error, reconnecting, and offline states where applicable.
- Accessibility semantics, dynamic font scaling, touch targets, keyboard/IME handling, and predictive back are first-class requirements.
- Prefer deterministic rendering and stable product theming over device-dependent visual drift.
- Do not add code only to satisfy a line-count target. Add code when it creates real capability, tests, platform integration, or maintainable separation.

## Product hierarchy

The primary flow is:

1. Conversation
2. Current run state
3. Tool/reasoning activity
4. Model/profile/connection context
5. Secondary management surfaces

The Android app should make the current agent run legible in one glance.

## Chat requirements

- Streaming assistant output with minimal layout churn.
- Markdown, links, code blocks, and attachments.
- Tool cards with lifecycle state and expandable output.
- Approval cards for dangerous/permissioned actions.
- Clarification prompts.
- Run controls: stop, pause/resume where supported, steer, retry, undo, branch, compact.
- Composer with attachments, model/profile selectors, and send/stop affordances.
- Preserve scroll position while new events arrive.
- Allow the user to inspect details without losing the primary conversational context.

## Connection requirements

- Multiple server profiles.
- Secure authentication/session storage.
- Reachability and health diagnostics.
- TLS-first public-network policy.
- Explicit private-network/Tailscale exceptions.
- Reconnect backoff.
- Offline read-only mode.

## Session requirements

- Search, pin, archive, rename, delete.
- Fast resume.
- Stable session identity.
- Offline cached transcript.
- Pagination/infinite scrolling.

## Workspace requirements

- Browse directories and files.
- Preview useful text/image formats.
- Show Git status where the backend supports it.
- Make destructive operations visually distinct and confirm them.

## Management requirements

- Models/providers.
- Profiles.
- Skills.
- Memory.
- Cron/tasks.
- Insights/usage.
- Settings.

## Android-native requirements

- Notifications for long-running runs.
- Android share target for text/files.
- Deep links to sessions.
- App shortcuts for new chat and recent sessions.
- Voice input/output hooks.
- Predictive back and proper IME behavior.
- Tablet/foldable two-pane layouts.
- System/light/dark themes and reduced-motion support.

## Quality gate

Before declaring a phase complete:

1. Compile debug and release.
2. Run unit tests.
3. Run Compose UI tests for changed critical paths.
4. Run lint.
5. Inspect generated APK metadata.
6. Verify no secrets or credentials were introduced.
7. Update `docs/PROJECT_STATE.md`.
8. Update parity notes if Hermes changed.

## Delivery gate

A release is not complete until GitHub Actions can build the APK/AAB and the release workflow can publish the artifacts to GitHub Releases. Production signing must use repository secrets rather than checked-in credentials.

## Engineering loop

For each feature:

**inspect → design → implement → test → review → integrate → document**

When a failure occurs, fix the underlying boundary instead of masking it in UI code.
