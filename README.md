# Hermex Android

A native Android client for [hermes-webui](https://github.com/hermes-webui/hermes-webui) — the self-hosted AI agent server.

## Features

- **Chat**: Full streaming chat with tool-call cards, reasoning blocks, and approval prompts
- **Sessions**: Browse, search, pin, archive, and manage conversation sessions
- **Models**: View and select from available LLM models
- **Profiles**: Switch between agent profiles
- **Tasks (Cron)**: Schedule and manage recurring AI tasks
- **Skills**: Browse and toggle installed skills
- **Workspace**: File browser for connected workspaces
- **Memory**: View and edit agent memory (memory, user, soul)
- **Insights**: Usage analytics and cost tracking
- **Multi-server**: Connect to multiple hermes-webui servers
- **Offline cache**: Cached sessions for read-only offline viewing

## Tech Stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Retrofit** + **OkHttp** for REST API
- **OkHttp EventSource** for SSE streaming
- **Room** for offline cache
- **DataStore** for preferences
- **Hilt** for dependency injection
- **Coroutines** + **Flow** for reactive programming

## Building

```bash
./gradlew assembleDebug
```

## Setup

1. Build and install the APK
2. Enter your hermes-webui server URL
3. Enter password if auth is enabled
4. Start chatting

## API Compatibility

This app targets the hermes-webui REST API. It supports:
- Password authentication (session cookie-based)
- Custom request headers for proxy/auth
- SSE streaming for real-time responses
- All major endpoints: sessions, models, profiles, cron, skills, workspace, memory, insights

## License

MIT
