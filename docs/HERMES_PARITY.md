# Hermes Desktop Parity Matrix

This document records current, externally verified product behavior that Optimus must match at the capability level.

## Desktop architecture

The official Hermes Desktop application is an Electron shell around a native React renderer. Electron resolves a runnable Hermes backend, owns native filesystem/git/window capabilities, and exposes a narrow preload bridge. The React renderer owns routes, panes, interaction state, and the assistant transcript. The backend is a headless `hermes serve` process exposing the TUI gateway JSON-RPC/WebSocket surface.

## Capability parity

| Desktop capability | Optimus native target | Status |
|---|---|---|
| Chat-first workspace | Native Compose chat-first workspace | In progress |
| Streaming responses | Explicit Android streaming state machine | In progress |
| Live tool activity | Expandable native tool cards | First UI slice |
| Structured tool summaries | Tool lifecycle domain model | Planned |
| Side-by-side previews | Tablet/landscape inspector pane | Planned |
| File browser | Native workspace feature | Existing baseline / hardening |
| Voice | Android-native voice integration | Planned |
| Sessions | Native sessions feature | Existing baseline / hardening |
| Search/archive | Native sessions feature | Existing baseline / hardening |
| Model picker | Native selector sheet | Existing baseline / hardening |
| Profiles | Native profile selector | Existing baseline / hardening |
| Skills | Native skills management | Existing baseline / hardening |
| Settings/onboarding | Native connection and settings flow | Existing baseline / hardening |
| Remote backend | Native HTTP/WebSocket adapter | Existing baseline / hardening |
| Local backend | Not assumed on Android | Product decision: remote-first |

## Important architectural distinction

Desktop owns local process lifecycle because it can install/start Hermes on the computer. Android should be a remote-first client: the agent runtime remains on the user-controlled Hermes host, while Android provides the high-quality control surface.

## Current upstream evidence

The official Hermes documentation describes the Desktop app as sharing the same agent, settings, API keys, sessions, skills, and memory as the CLI/gateway. It is chat-first and includes streaming tool output, side-by-side previews, a file browser, voice, cron, profiles, skills, settings, and remote backend support.

The Hermes repository also describes Desktop as Electron + React with the renderer communicating with a headless `hermes serve` backend over JSON-RPC/WebSocket.

## Mobile lessons from existing clients

Existing Hermes mobile implementations demonstrate two useful patterns:

1. A native client can be implemented with Kotlin/Jetpack Compose while retaining the Hermes backend as the source of truth.
2. A WebView/Capacitor approach can reuse the Desktop renderer quickly, but it couples mobile UX to desktop DOM/layout and does not meet Optimus' native-first quality bar.

Optimus therefore uses native Kotlin/Compose and reimplements the desktop mental model rather than wrapping the desktop renderer.
