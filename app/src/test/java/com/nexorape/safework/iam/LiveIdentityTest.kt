package com.nexorape.safework.iam

import com.google.gson.Gson
import com.nexorape.safework.core.network.ApiClient
import com.nexorape.safework.iam.application.IdentityUseCases
import com.nexorape.safework.iam.domain.repositories.*
import com.nexorape.safework.iam.infrastructure.http.HttpIdentityRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.util.UUID
import java.util.Collections
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient

/** Opt-in synthetic HTTP checks against the pinned current-course local server, never history. */
class LiveIdentityTest {
    @Test fun invitationLoginProfileRestoreIndependentLogoutAndRevocation() = runBlocking {
        val url = System.getenv("SAFEWORK_LIVE_API_URL")
        assumeTrue("Local backend integration requires explicit configuration", !url.isNullOrBlank())
        require(url!!.startsWith("http://127.0.0.1:"))
        val adminEmail = System.getenv("SAFEWORK_TEST_ADMIN_EMAIL") ?: error("Missing synthetic operator fixture")
        val adminPassword = System.getenv("SAFEWORK_TEST_ADMIN_PASSWORD") ?: error("Missing synthetic operator fixture")
        val evidence = Collections.synchronizedList(mutableListOf<Map<String, Any>>())
        val transport = OkHttpClient.Builder().followRedirects(false).followSslRedirects(false)
            .retryOnConnectionFailure(false).callTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val response = chain.proceed(chain.request())
                evidence.add(mapOf("method" to chain.request().method,
                    "path" to chain.request().url.encodedPath, "status" to response.code))
                response
            }.build()
        fun api(store: MemorySessions) = ApiClient(url, true, store, transport)
        val adminStore = MemorySessions()
        val adminApi = api(adminStore)
        val admin = IdentityUseCases(HttpIdentityRepository(adminApi, adminStore))
        val operator = admin.login(adminEmail, adminPassword)
        val email = UUID.randomUUID().toString() + "@example.test"
        val password = "Synthetic-" + UUID.randomUUID().toString()
        val gson = Gson()
        val invitation = adminApi.request("POST", "api/v1/companies/${operator.companyId.value}/invitations",
            gson.toJson(mapOf("emailAddress" to email)), expected = 201)!!.asJsonObject["invitationToken"].asString
        val firstStore = MemorySessions()
        val first = IdentityUseCases(HttpIdentityRepository(api(firstStore), firstStore))
        val worker = first.register("Synthetic Mobile Worker", email, password, invitation)
        assertEquals(setOf("WORKER"), worker.roles.map { it.name }.toSet()); assertNull(firstStore.read())
        val signed = first.login(email, password)
        assertEquals(worker.id, signed.id)
        assertEquals(signed, first.restore())
        val updated = first.updateProfile("Synthetic Mobile Edited", "+51 900 000 000")
        assertEquals("Synthetic Mobile Edited", updated.fullName.value)
        assertEquals("+51 900 000 000", updated.phone?.value)
        val secondStore = MemorySessions()
        val second = IdentityUseCases(HttpIdentityRepository(api(secondStore), secondStore))
        second.login(email, password)
        expect(IdentityFailure.INVALID_CREDENTIALS) { second.login(email, "Synthetic-wrong-password") }
        assertEquals(updated, second.profile())
        first.logout()
        assertNull(firstStore.read()); assertNull(first.restore())
        assertEquals(updated, second.profile())
        adminApi.request("PATCH", "api/v1/administration/users/${worker.id.value}/roles",
            gson.toJson(mapOf("roles" to listOf("WORKER", "EMPLOYER"))))
        expect(IdentityFailure.SESSION_INVALID) { second.profile() }
        assertNull(secondStore.read())
        assertEquals(setOf("WORKER", "EMPLOYER"), second.login(email, password).roles.map { it.name }.toSet())
        second.logout(); admin.logout()
        println("Live IAM HTTP results (methods/paths/statuses only): " + gson.toJson(evidence))
    }

    private suspend fun expect(reason: IdentityFailure, action: suspend () -> Any?) {
        try { action(); fail("Expected rejection") } catch (e: IdentityException) { assertEquals(reason, e.reason) }
    }
}
