# Upstream Hermes Tracking

## Verified current desktop model

As of August 2026, official Hermes Desktop is a native Electron application with a React renderer. It launches or connects to a headless Hermes backend and uses the TUI gateway JSON-RPC/WebSocket API. Desktop and CLI share the same agent state, configuration, sessions, skills, and memory.

## Mobile research conclusions

- WebView/Capacitor implementations are useful reference implementations for transport and feature parity, but they couple mobile behavior to desktop renderer internals.
- Native Kotlin/Compose clients demonstrate better Android ergonomics for navigation, lifecycle, storage, notifications, and adaptive layouts.
- The strongest mobile information architectures treat chat, agent activity, and account/settings surfaces as first-class tabs or panes while preserving a single coherent agent identity.

## Update procedure

When upstream Hermes changes:

1. Inspect `apps/desktop/README.md` and `AGENTS.md`.
2. Inspect `apps/shared` transport contracts.
3. Inspect the current gateway/server protocol.
4. Compare changed Desktop capabilities against `docs/HERMES_PARITY.md`.
5. Update domain adapters before changing Compose screens.
6. Add regression fixtures/tests for changed event shapes.
7. Update `docs/PROJECT_STATE.md`.

Never silently copy undocumented endpoint shapes from third-party mobile clients.
