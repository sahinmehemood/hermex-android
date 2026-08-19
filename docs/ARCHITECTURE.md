# Optimus Architecture

## High-level

```text
Android App
│
├── Presentation
│   ├── Compose screens
│   ├── navigation
│   ├── screen state
│   ├── UI components
│   └── motion/accessibility
│
├── Domain
│   ├── session orchestration
│   ├── agent run state machine
│   ├── tool lifecycle
│   ├── approvals
│   ├── connection policy
│   └── use cases
│
├── Data
│   ├── Hermes API adapters
│   ├── WebSocket/SSE transport
│   ├── Room cache
│   ├── DataStore preferences
│   ├── secure credential store
│   └── DTO → domain mapping
│
└── Android platform
    ├── notifications
    ├── share intents
    ├── shortcuts
    ├── voice
    ├── files/media
    └── background lifecycle
```

## Recommended package structure

```text
com.optimus.hermes
├── app
├── core
│   ├── common
│   ├── logging
│   ├── result
│   ├── time
│   └── platform
├── data
│   ├── api
│   ├── websocket
│   ├── database
│   ├── preferences
│   ├── secure
│   └── repository
├── domain
│   ├── connection
│   ├── sessions
│   ├── chat
│   ├── tools
│   ├── workspace
│   ├── skills
│   ├── memory
│   ├── tasks
│   ├── models
│   └── insights
└── feature
    ├── onboarding
    ├── home
    ├── sessions
    ├── chat
    ├── workspace
    ├── skills
    ├── memory
    ├── tasks
    ├── models
    ├── insights
    └── settings
```

## Streaming state machine

The agent run must have explicit states:

```text
Idle
  ↓
Submitting
  ↓
Running ────────────────┐
  │                     │
  ├─ ToolStarted        │
  ├─ ToolProgress       │
  ├─ ToolAwaitingApproval
  ├─ ToolCompleted       │
  ├─ Clarification       │
  └─ TextDelta           │
                        ↓
                     Completed
                        │
             ┌──────────┴──────────┐
             ↓                     ↓
           Failed                Cancelled
```

The UI derives visuals from this state rather than transport callbacks directly.

## Repository rules

- Server DTOs never leak into Compose.
- Composables do not perform network calls directly.
- ViewModels expose immutable `StateFlow` state.
- Repositories own retry and cache policy.
- Logging is structured and redacts credentials/tokens.
- All destructive workspace/session actions require explicit intent.
