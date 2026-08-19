# Optimus — Hermes Native Android Client

## Product goal

Build a premium, production-grade native Android client for Hermes Agent that preserves the mental model and visual language of the official Hermes desktop client while adopting the interaction quality of excellent native iOS/Android clients.

## Non-goals

- Do not embed the desktop UI in a WebView.
- Do not fake feature completeness by generating boilerplate or meaningless code.
- Do not hard-code undocumented backend contracts.
- Do not store secrets in plain-text preferences, logs, source control, or crash reports.

## Core user journeys

1. Connect to a remote Hermes instance securely.
2. Browse and resume sessions.
3. Start a new agent run and watch streamed output in real time.
4. Inspect reasoning/tool activity without losing the conversational flow.
5. Approve, reject, clarify, stop, steer, retry, branch, or compact a run.
6. Attach files/images/audio from Android.
7. Browse workspace files and Git state.
8. Manage models, profiles, skills, memory, and scheduled tasks.
9. Continue viewing cached sessions when connectivity is unavailable.
10. Receive useful Android notifications while a long-running agent task progresses.

## UX bar

- Native Jetpack Compose UI.
- iOS-quality motion: spring-based transitions, contextual sheets, progressive disclosure, meaningful haptics.
- Hermes desktop information hierarchy: conversation first, operational context second, controls immediately reachable.
- Material 3 semantics without looking like a generic Material template.
- Phone, large phone, tablet, and foldable support.
- Dynamic type / font scaling and accessibility semantics throughout.
- Dark, light, and system appearance.
- Reduced-motion mode.

## Functional areas

### Connection
- Multiple server profiles.
- Reachability/health probe.
- Authentication/session negotiation.
- Secure credential storage.
- TLS-first defaults with explicit private-network exceptions.
- Connection diagnostics.

### Chat
- Streaming text.
- Markdown and code rendering.
- Tool lifecycle timeline.
- Reasoning blocks.
- Approval/clarification cards.
- Run controls: stop, steer, retry, undo, compact, branch.
- Model/profile/reasoning selectors.
- Attachments and previews.
- Voice input/output hooks.

### Sessions
- Infinite-scroll history.
- Search.
- Pin/archive/rename/delete.
- Project/group metadata where supported.
- Offline cached transcripts.

### Workspace
- Directory browser.
- File preview.
- Search.
- Git status/diffs/branch operations where the backend contract supports them.
- Destructive-action confirmations.

### Agent management
- Models/providers.
- Profiles.
- Skills.
- Memory.
- Scheduled tasks / cron.
- Usage insights.

### Android integration
- Notifications.
- Share sheet.
- App shortcuts.
- Deep links.
- Predictive back.
- Clipboard/share actions.
- Optional home-screen widget.

## Quality gates

Every feature should have:

- unit coverage for domain logic;
- state/reducer tests for non-trivial UI behavior;
- Compose UI tests for critical interaction paths;
- network contract tests or fixtures;
- accessibility checks where practical;
- loading/empty/error/offline states;
- analytics-free error reporting suitable for privacy-first operation.

## Architecture principles

- Separate UI, domain, data, and platform layers.
- Prefer immutable state and unidirectional data flow.
- Keep server DTOs isolated from UI models.
- Make connection/session identity explicit.
- Treat streaming as a first-class state machine rather than ad-hoc callbacks.
- Make offline cache read-only unless safe conflict semantics are explicitly implemented.
- Design backend adapters so Hermes protocol evolution does not cascade through the UI.

## Code size policy

There is no artificial line-count requirement. The repository should grow to the size required by real functionality, tests, documentation, assets, generated resources, and maintainable abstraction boundaries. Quality and capability are the measurable targets.

## Current execution baseline

The existing repository already contains an initial Kotlin/Compose Hermes client. The Optimus branch is the long-lived hardening/rearchitecture line and should preserve useful work while replacing weak foundations incrementally.
