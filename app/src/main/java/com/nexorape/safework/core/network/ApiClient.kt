package com.nexorape.safework.core.network

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ApiException(val status: Int, val code: String) : RuntimeException(code)

/** No request/response/header logging, redirects, fallback host or automatic retries. */
class ApiClient(
    rawUrl: String,
    debug: Boolean,
    private val sessions: SessionStore,
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS).followRedirects(false).followSslRedirects(false)
        .retryOnConnectionFailure(false).build(),
) {
    val baseUrl: String
    init {
        val url = rawUrl.toHttpUrlOrNull()
        val local = url?.host in setOf("127.0.0.1", "localhost", "10.0.2.2")
        baseUrl = if (url != null && url.username.isEmpty() && url.password.isEmpty() &&
            url.query == null && url.fragment == null && url.encodedPath == "/" &&
            (url.isHttps || debug && local)) url.toString() else ""
    }

    suspend fun request(method: String, path: String, body: String? = null, authenticated: Boolean = true,
                        tokenOverride: String? = null, expected: Int = 200): JsonElement? {
        if (baseUrl.isEmpty()) throw ApiException(0, "NOT_CONFIGURED")
        val credential = if (authenticated && tokenOverride == null) sessions.read() else null
        if (credential != null && credential.apiUrl != baseUrl) {
            sessions.clear(credential.token)
            throw ApiException(401, "SESSION_INVALID")
        }
        val token = tokenOverride ?: credential?.token
        if (authenticated && token.isNullOrBlank()) throw ApiException(401, "SESSION_INVALID")
        require(path.startsWith("api/v1/") && '?' !in path && '#' !in path)
        val builder = Request.Builder().url(baseUrl + path).header("Accept", "application/json")
        if (authenticated) builder.header("Authorization", "Bearer $token")
        val payload = body?.toRequestBody("application/json; charset=utf-8".toMediaType())
            ?: if (method == "POST" || method == "PATCH") ByteArray(0).toRequestBody(null) else null
        builder.method(method, payload)
        val response = try { client.newCall(builder.build()).await() }
        catch (_: IOException) { throw ApiException(0, "NETWORK") }
        return response.use {
            if (it.code == 401 && authenticated && tokenOverride == null) sessions.clear(token)
            if (it.code != expected) {
                // Interpret only whitelisted code; never expose arbitrary server message/body.
                val code = when (it.code) {
                    400 -> "INVALID_INPUT"; 401 -> if (authenticated) "SESSION_INVALID" else "INVALID_CREDENTIALS"
                    403 -> "FORBIDDEN"; 409 -> "EMAIL_UNAVAILABLE"; 422 -> "INVITATION_INVALID"
                    429 -> "RATE_LIMITED"; else -> "SERVER"
                }
                throw ApiException(it.code, code)
            }
            if (expected == 204) return@use null
            val responseBody = it.body ?: throw ApiException(0, "INVALID_RESPONSE")
            if (responseBody.source().request(1_048_577)) throw ApiException(0, "INVALID_RESPONSE")
            try { JsonParser.parseString(responseBody.string()).also { json ->
                if (json.isJsonNull) throw ApiException(0, "INVALID_RESPONSE")
            } } catch (_: RuntimeException) { throw ApiException(0, "INVALID_RESPONSE") }
        }
    }

    private suspend fun Call.await(): Response = suspendCancellableCoroutine { continuation ->
        continuation.invokeOnCancellation { cancel() }
        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (!continuation.isCancelled) continuation.resumeWithException(e)
            }
            override fun onResponse(call: Call, response: Response) {
                if (continuation.isCancelled) response.close()
                else continuation.resume(response) { _, value, _ -> value.close() }
            }
        })
    }
}
