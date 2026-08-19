package com.hermex.data.gateway

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

/**
 * Native JSON-RPC/WebSocket transport for the Hermes TUI gateway.
 *
 * It intentionally mirrors the upstream protocol shape:
 *   { jsonrpc: "2.0", id, method, params }
 * and asynchronous gateway events arrive as:
 *   { jsonrpc: "2.0", method: "event", params: { type, payload, session_id } }
 */
class HermesGatewayTransport(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(20, TimeUnit.SECONDS)
        .build(),
    private val gson: Gson = Gson()
) {
    private val nextId = AtomicLong(0)
    private val pending = ConcurrentHashMap<String, CompletableDeferred<JsonElement>>()
    private val _events = MutableSharedFlow<GatewayEvent>(extraBufferCapacity = 512)
    val events: Flow<GatewayEvent> = _events.asSharedFlow()

    @Volatile
    private var socket: WebSocket? = null

    @Volatile
    private var connectedUrl: String? = null

    suspend fun connect(wsUrl: String) = withContext(Dispatchers.IO) {
        require(wsUrl.startsWith("ws://") || wsUrl.startsWith("wss://")) {
            "Hermes gateway URL must start with ws:// or wss://"
        }

        if (socket != null && connectedUrl == wsUrl) return@withContext
        disconnect()

        val opened = CompletableDeferred<Unit>()
        val request = Request.Builder().url(wsUrl).build()
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                socket = webSocket
                connectedUrl = wsUrl
                if (!opened.isCompleted) opened.complete(Unit)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleFrame(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(code, reason)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (socket === webSocket) {
                    socket = null
                    connectedUrl = null
                }
                rejectPending(IllegalStateException("Hermes gateway closed: $reason"))
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                if (socket === webSocket) {
                    socket = null
                    connectedUrl = null
                }
                if (!opened.isCompleted) opened.completeExceptionally(t)
                rejectPending(t)
            }
        }

        client.newWebSocket(request, listener)
        opened.await()
    }

    fun disconnect() {
        socket?.close(1000, "client disconnect")
        socket = null
        connectedUrl = null
        rejectPending(IllegalStateException("Hermes gateway disconnected"))
    }

    suspend fun <T> request(
        method: String,
        params: Map<String, Any?> = emptyMap(),
        timeoutMs: Long = 30_000L,
        responseType: Class<T>
    ): T {
        val ws = socket ?: throw IllegalStateException("Hermes gateway is not connected")
        val id = nextId.incrementAndGet().toString()
        val deferred = CompletableDeferred<JsonElement>()
        pending[id] = deferred

        val frame = JsonObject().apply {
            addProperty("jsonrpc", "2.0")
            addProperty("id", id)
            addProperty("method", method)
            add("params", gson.toJsonTree(params))
        }

        if (!ws.send(frame.toString())) {
            pending.remove(id)
            throw IllegalStateException("Hermes gateway refused RPC frame")
        }

        return try {
            kotlinx.coroutines.withTimeout(timeoutMs) {
                gson.fromJson(deferred.await(), responseType)
            }
        } catch (error: CancellationException) {
            pending.remove(id)
            throw error
        } catch (error: Throwable) {
            pending.remove(id)
            throw error
        }
    }

    private fun handleFrame(text: String) {
        val frame = runCatching { JsonParser.parseString(text).asJsonObject }.getOrNull() ?: return

        if (frame.has("id") && !frame.get("id").isJsonNull) {
            val id = frame.get("id").asString
            val call = pending.remove(id) ?: return
            if (frame.has("error")) {
                val error = frame.getAsJsonObject("error")
                val message = error.get("message")?.asString ?: "Hermes RPC failed"
                call.completeExceptionally(IllegalStateException(message))
            } else {
                call.complete(frame.get("result") ?: JsonObject())
            }
            return
        }

        if (frame.get("method")?.asString != "event") return
        val params = frame.getAsJsonObject("params") ?: return
        val payload = params.get("payload")
        _events.tryEmit(
            GatewayEvent(
                type = params.get("type")?.asString ?: return,
                sessionId = params.get("session_id")?.takeUnless { it.isJsonNull }?.asString,
                payload = payload
            )
        )
    }

    private fun rejectPending(error: Throwable) {
        pending.values.forEach { it.completeExceptionally(error) }
        pending.clear()
    }
}

data class GatewayEvent(
    val type: String,
    val sessionId: String?,
    val payload: JsonElement?
)
