package com.hermex.data.gateway

import com.google.gson.JsonElement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HermesSessionGateway(
    private val connectionStore: ConnectionStore,
    private val transport: HermesGatewayTransport
) {
    suspend fun createSession(
        title: String = "",
        model: String? = null,
        profile: String? = null
    ): CreatedSession = withContext(Dispatchers.IO) {
        ensureConnected()
        val params = linkedMapOf<String, Any?>(
            "title" to title,
            "model" to model,
            "profile" to profile,
            "cols" to 100
        ).filterValues { it != null }
        val result = transport.request<JsonElement>("session.create", params, responseType = JsonElement::class.java)
        CreatedSession(
            id = result.asJsonObject.get("session_id")?.asString ?: error("Hermes did not return a session_id"),
            title = result.asJsonObject.getAsJsonObject("info")?.get("title")?.asString ?: title,
            model = result.asJsonObject.getAsJsonObject("info")?.get("model")?.asString ?: model,
            profile = result.asJsonObject.getAsJsonObject("info")?.get("profile_name")?.asString ?: profile
        )
    }

    suspend fun resume(sessionId: String): SessionTranscript = withContext(Dispatchers.IO) {
        ensureConnected()
        val result = transport.request<JsonElement>(
            "session.resume",
            mapOf("session_id" to sessionId, "cols" to 100, "defer_history" to false),
            responseType = JsonElement::class.java
        )
        val rows = result.asJsonObject.get("messages")?.takeIf { it.isJsonArray }?.asJsonArray.orEmpty()
        SessionTranscript(
            sessionId = result.asJsonObject.get("session_id")?.asString ?: sessionId,
            messages = rows.mapNotNull { row ->
                if (!row.isJsonObject) return@mapNotNull null
                val obj = row.asJsonObject
                val role = obj.get("role")?.asString ?: return@mapNotNull null
                val text = obj.get("content")?.asString ?: return@mapNotNull null
                TranscriptMessage(role, text)
            }
        )
    }

    private suspend fun ensureConnected() {
        val settings = connectionStore.settings()
        require(settings.wsUrl.isNotBlank()) { "No Hermes WebSocket URL configured" }
        transport.connect(settings.wsUrl)
    }
}

data class CreatedSession(
    val id: String,
    val title: String,
    val model: String?,
    val profile: String?
)

data class SessionTranscript(
    val sessionId: String,
    val messages: List<TranscriptMessage>
)

data class TranscriptMessage(
    val role: String,
    val text: String
)
