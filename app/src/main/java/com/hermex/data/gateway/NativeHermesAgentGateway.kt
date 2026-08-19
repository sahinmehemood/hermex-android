package com.hermex.data.gateway

import com.google.gson.JsonElement
import com.hermex.domain.chat.AgentGateway
import com.hermex.domain.chat.AgentRunEvent
import com.hermex.domain.chat.ApprovalRequest
import com.hermex.domain.chat.AttachmentRef
import com.hermex.domain.chat.ClarificationRequest
import com.hermex.domain.chat.ToolRun
import com.hermex.domain.chat.ToolStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import kotlinx.coroutines.channels.awaitClose

/**
 * Maps the upstream Hermes gateway event stream into the transport-independent
 * Optimus agent run state machine.
 */
class NativeHermesAgentGateway(
    private val connectionStore: ConnectionStore,
    private val transport: HermesGatewayTransport
) : AgentGateway {

    override suspend fun submitPrompt(
        sessionId: String,
        prompt: String,
        attachments: List<AttachmentRef>,
        model: String?,
        profile: String?
    ): Flow<AgentRunEvent> = channelFlow {
        ensureConnected()

        val params = linkedMapOf<String, Any?>(
            "session_id" to sessionId,
            "text" to prompt,
            "profile" to profile,
            "model" to model,
            "attachments" to attachments.map {
                mapOf(
                    "id" to it.id,
                    "name" to it.name,
                    "mime_type" to it.mimeType,
                    "size_bytes" to it.sizeBytes,
                    "remote_path" to it.remotePath
                )
            }
        ).filterValues { it != null }

        val collector = kotlinx.coroutines.launch {
            transport.events.collect { event ->
                if (event.sessionId != null && event.sessionId != sessionId) return@collect
                val mapped = mapEvent(event) ?: return@collect
                send(mapped)
                if (mapped is AgentRunEvent.Completed || mapped is AgentRunEvent.Failed || mapped is AgentRunEvent.Cancelled) {
                    close()
                }
            }
        }

        try {
            transport.request("prompt.submit", params, timeoutMs = 1_800_000L, responseType = JsonElement::class.java)
            awaitClose { collector.cancel() }
        } catch (error: Throwable) {
            collector.cancel()
            throw error
        }
    }

    override suspend fun stopRun(sessionId: String) {
        ensureConnected()
        transport.request<JsonElement>(
            "session.interrupt",
            mapOf("session_id" to sessionId),
            responseType = JsonElement::class.java
        )
    }

    override suspend fun steerRun(sessionId: String, instruction: String): Flow<AgentRunEvent> =
        submitPrompt(sessionId, instruction, emptyList(), null, null)

    override suspend fun respondToApproval(sessionId: String, requestId: String, approved: Boolean): Flow<AgentRunEvent> =
        respondToInput(
            sessionId = sessionId,
            method = "approval.respond",
            params = mapOf(
                "choice" to if (approved) "once" else "deny",
                "request_id" to requestId,
                "session_id" to sessionId
            )
        )

    override suspend fun respondToClarification(sessionId: String, requestId: String, answer: String): Flow<AgentRunEvent> =
        respondToInput(
            sessionId = sessionId,
            method = "clarify.respond",
            params = mapOf(
                "request_id" to requestId,
                "answer" to answer,
                "session_id" to sessionId
            )
        )

    private suspend fun respondToInput(
        sessionId: String,
        method: String,
        params: Map<String, Any?>
    ): Flow<AgentRunEvent> = channelFlow {
        ensureConnected()
        val collector = kotlinx.coroutines.launch {
            transport.events.collect { event ->
                if (event.sessionId != null && event.sessionId != sessionId) return@collect
                mapEvent(event)?.let { send(it) }
            }
        }
        try {
            transport.request<JsonElement>(method, params, responseType = JsonElement::class.java)
            awaitClose { collector.cancel() }
        } catch (error: Throwable) {
            collector.cancel()
            throw error
        }
    }

    private suspend fun ensureConnected() = withContext(Dispatchers.IO) {
        val settings = connectionStore.settings()
        require(settings.wsUrl.isNotBlank()) { "No Hermes WebSocket URL configured" }
        val wsUrl = addToken(settings.wsUrl, settings.token)
        transport.connect(wsUrl)
    }

    private fun addToken(url: String, token: String?): String {
        if (token.isNullOrBlank()) return url
        return runCatching {
            val parsed = java.net.URI(url)
            val existing = parsed.rawQuery.orEmpty()
            if (existing.contains("token=") || existing.contains("ticket=")) return@runCatching url
            val encoded = java.net.URLEncoder.encode(token, Charsets.UTF_8.name())
            val separator = if (existing.isEmpty()) "" else "&"
            java.net.URI(
                parsed.scheme,
                parsed.userInfo,
                parsed.host,
                parsed.port,
                parsed.path,
                "$existing${separator}token=$encoded",
                parsed.fragment
            ).toString()
        }.getOrDefault(url)
    }

    private fun mapEvent(event: GatewayEvent): AgentRunEvent? {
        val p = event.payload
        return when (event.type) {
            "gateway.ready" -> null
            "message.start" -> AgentRunEvent.Started(event.sessionId ?: "unknown")
            "message.delta" -> AgentRunEvent.TextDelta(readString(p, "delta") ?: readString(p, "text") ?: readString(p, "content") ?: "")
            "message.interim" -> AgentRunEvent.TextDelta(readString(p, "text") ?: readString(p, "content") ?: "")
            "thinking.delta", "reasoning.delta" -> AgentRunEvent.ThinkingDelta(readString(p, "delta") ?: readString(p, "text") ?: "")
            "tool.start" -> AgentRunEvent.ToolStarted(
                ToolRun(
                    id = readString(p, "tool_id") ?: readString(p, "id") ?: "tool-${System.nanoTime()}",
                    name = readString(p, "name") ?: readString(p, "tool") ?: "Tool",
                    inputSummary = readString(p, "input") ?: readString(p, "args") ?: "",
                    status = ToolStatus.Running,
                    startedAtEpochMs = System.currentTimeMillis()
                )
            )
            "tool.progress", "tool.generating" -> AgentRunEvent.ToolOutput(
                toolId = readString(p, "tool_id") ?: readString(p, "id") ?: "",
                output = readString(p, "output") ?: readString(p, "text") ?: readString(p, "delta") ?: ""
            )
            "tool.complete" -> AgentRunEvent.ToolCompleted(
                toolId = readString(p, "tool_id") ?: readString(p, "id") ?: "",
                output = readString(p, "output") ?: readString(p, "result") ?: ""
            )
            "tool.failed" -> AgentRunEvent.ToolFailed(
                toolId = readString(p, "tool_id") ?: readString(p, "id") ?: "",
                output = readString(p, "output") ?: readString(p, "error") ?: ""
            )
            "approval.request" -> AgentRunEvent.ApprovalRequired(
                ApprovalRequest(
                    id = readString(p, "request_id") ?: "approval-${System.nanoTime()}",
                    title = readString(p, "description") ?: "Approval required",
                    detail = readString(p, "command") ?: readString(p, "description") ?: "The agent is waiting for approval.",
                    danger = true
                )
            )
            "clarify.request" -> AgentRunEvent.ClarificationRequired(
                ClarificationRequest(
                    id = readString(p, "request_id") ?: "clarify-${System.nanoTime()}",
                    question = readString(p, "question") ?: "Hermes needs more information.",
                    options = p?.let { jsonArrayStrings(it, "choices") } ?: emptyList()
                )
            )
            "message.complete", "background.complete" -> AgentRunEvent.Completed
            "error" -> AgentRunEvent.Failed(readString(p, "message") ?: "Hermes gateway error", recoverable = true)
            else -> null
        }
    }

    private fun readString(payload: JsonElement?, key: String): String? {
        if (payload == null || !payload.isJsonObject) return null
        val value = payload.asJsonObject.get(key) ?: return null
        return if (value.isJsonPrimitive && value.asJsonPrimitive.isString) value.asString else value.toString()
    }

    private fun jsonArrayStrings(payload: JsonElement, key: String): List<String> {
        if (!payload.isJsonObject) return emptyList()
        val array = payload.asJsonObject.get(key)?.takeIf { it.isJsonArray } ?: return emptyList()
        return array.asJsonArray.mapNotNull { if (it.isJsonPrimitive) it.asString else null }
    }
}
