# Optimus Project State

## Current status

Phase: foundation + architecture hardening.

### Completed in this branch

- Confirmed GitHub write access through the connected GitHub integration.
- Created development branch `optimus-android` from `main`.
- Added `PROJECT_SPEC.md` as the product source of truth.
- Added `docs/ARCHITECTURE.md`.
- Added `docs/DESIGN_SYSTEM.md`.
- Added Android validation workflow: `.github/workflows/android.yml`.
- Added release workflow: `.github/workflows/release.yml`.
- Added Gradle version override and CI signing support in `app/build.gradle.kts`.
- Existing initial Hermes Android implementation remains the functional baseline under the branch.

## Product north star

Native Android implementation inspired by the official Hermes desktop experience and excellent native iOS clients, with Android-first interaction patterns and a premium visual system.

## Next engineering slices

1. Rename/rebrand from Hermex to Optimus while keeping backend compatibility.
2. Introduce layered `core/data/domain/feature` package boundaries.
3. Replace ad-hoc API calls with typed repositories and explicit Result/error policy.
4. Implement a formal streaming run state machine.
5. Rebuild the primary adaptive navigation shell.
6. Rebuild chat transcript, tool events, approvals, and composer.
7. Harden secure credential/session storage.
8. Add Room-backed session/message cache and offline state.
9. Add workspace and Git interaction safety model.
10. Add skills/memory/tasks/models/profiles/insights hardening.
11. Add critical path Compose/UI/integration tests.
12. Run Android CI and fix every build/lint/test failure.
13. Configure a production release keystore in GitHub Secrets.
14. Publish the first real Optimus APK/AAB release.

## Important constraint

Do not inflate the project with meaningless code merely to reach a line-count target. Increase code volume only when it represents real capability, tests, abstractions, documentation, or platform integration.

## Operational memory

When work resumes, read this file plus `PROJECT_SPEC.md`, `docs/ARCHITECTURE.md`, and `docs/DESIGN_SYSTEM.md` before making structural changes. Keep this file updated after each major phase.
