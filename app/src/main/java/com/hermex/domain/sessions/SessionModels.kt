package com.hermex.domain.sessions

import java.time.Instant

data class HermesSession(
    val id: String,
    val title: String,
    val project: String? = null,
    val profile: String? = null,
    val model: String? = null,
    val updatedAt: Instant? = null,
    val messageCount: Int = 0,
    val pinned: Boolean = false,
    val archived: Boolean = false,
    val source: SessionSource = SessionSource.Hermes
)

enum class SessionSource {
    Hermes,
    Desktop,
    Cli,
    ScheduledTask,
    Messaging,
    Unknown
}

data class SessionPage(
    val items: List<HermesSession>,
    val nextCursor: String? = null
)

sealed interface SessionMutation {
    data class Rename(val sessionId: String, val title: String) : SessionMutation
    data class Pin(val sessionId: String, val value: Boolean) : SessionMutation
    data class Archive(val sessionId: String, val value: Boolean) : SessionMutation
    data class Delete(val sessionId: String) : SessionMutation
}

interface SessionRepository {
    suspend fun list(query: String? = null, cursor: String? = null): SessionPage
    suspend fun get(id: String): HermesSession?
    suspend fun mutate(mutation: SessionMutation)
    suspend fun cache(session: HermesSession)
}
