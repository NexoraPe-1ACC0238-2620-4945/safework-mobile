package com.nexorape.safework.iam

import com.google.gson.JsonParser
import com.nexorape.safework.core.network.*
import com.nexorape.safework.iam.application.IdentityUseCases
import com.nexorape.safework.iam.domain.repositories.*
import com.nexorape.safework.iam.infrastructure.http.HttpIdentityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

internal class MemorySessions : SessionStore {
    private var credential: SessionCredential? = null
    override val invalidations = MutableStateFlow(0)
    override fun read() = credential
    override fun write(credential: SessionCredential) { this.credential = credential }
    override fun clear(expectedToken: String?) {
        if (expectedToken == null || expectedToken == credential?.token) { credential = null; invalidations.value++ }
    }
}

class IdentityHttpTest {
    private lateinit var server: MockWebServer
    private lateinit var store: MemorySessions
    private lateinit var api: ApiClient
    private lateinit var useCases: IdentityUseCases
    private val profile = """{"id":1,"companyId":2,"fullName":"Synthetic Worker","email":"synthetic@example.test","phoneNumber":null,"createdAt":"2026-10-08T12:00:00Z","updatedAt":"2026-10-08T12:00:00Z","roles":["WORKER"]}"""
    @Before fun setup() {
        server = MockWebServer().apply { start() }
        store = MemorySessions()
        api = ApiClient(server.url("/").toString(), true, store)
        useCases = IdentityUseCases(HttpIdentityRepository(api, store))
    }
    @After fun teardown() { server.shutdown() }
    private fun respond(code: Int, body: String = "") { server.enqueue(MockResponse().setResponseCode(code).setBody(body)) }

    @Test fun loginFetchesOwnProfileBeforeSavingAndRestoreRevalidates() = runBlocking {
        respond(200, """{"id":1,"username":"synthetic@example.test","token":"synthetic-invalid-token"}""")
        respond(200, profile)
        val user = useCases.login(" Synthetic@Example.Test ", "SyntheticPass12")
        assertEquals(2L, user.companyId.value)
        val login = server.takeRequest()
        assertEquals("/api/v1/authentication/sign-in", login.path)
        assertNull(login.getHeader("Authorization"))
        assertEquals("synthetic@example.test", JsonParser.parseString(login.body.readUtf8()).asJsonObject["email"].asString)
        val me = server.takeRequest()
        assertEquals("Bearer synthetic-invalid-token", me.getHeader("Authorization"))
        assertNotNull(store.read())
        respond(200, profile)
        assertEquals(user, useCases.restore())
        assertEquals("/api/v1/users/me", server.takeRequest().path)
    }

    @Test fun signupOnlySendsInvitationAndNeverPersistsALogin() = runBlocking {
        respond(201, profile)
        val user = useCases.register("Synthetic Worker", "synthetic@example.test", "SyntheticPass12", "synthetic-invalid-proof")
        assertEquals("WORKER", user.roles.single().name)
        val request = server.takeRequest()
        val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject
        assertEquals(setOf("fullName", "emailAddress", "password", "invitationToken"), body.keySet())
        assertNull(request.getHeader("Authorization")); assertNull(store.read())
    }

    @Test fun invalidLoginDoesNotPersistCredentials() = runBlocking {
        respond(401, """{"code":"INVALID_CREDENTIALS"}""")
        fails(IdentityFailure.INVALID_CREDENTIALS) { useCases.login("synthetic@example.test", "SyntheticPass12") }
        assertNull(store.read())
    }

    @Test fun revokedSessionClearsStorageButForbiddenSessionSurvives() = runBlocking {
        store.write(SessionCredential("synthetic-invalid-token", api.baseUrl))
        respond(403)
        fails(IdentityFailure.FORBIDDEN) { useCases.profile() }
        assertNotNull(store.read())
        respond(401)
        fails(IdentityFailure.SESSION_INVALID) { useCases.restore() }
        assertNull(store.read()); assertEquals(1, store.invalidations.value)
    }

    @Test fun logoutUsesNoPayloadAndOnlyClearsAfterSuccessOrInvalidSession() = runBlocking {
        store.write(SessionCredential("synthetic-invalid-token", api.baseUrl))
        respond(500)
        fails(IdentityFailure.SERVER) { useCases.logout() }
        assertNotNull(store.read())
        respond(204)
        useCases.logout()
        server.takeRequest()
        val logout = server.takeRequest()
        assertEquals("POST", logout.method); assertEquals(0L, logout.bodySize)
        assertNull(store.read())
    }

    @Test fun patchOmitsBlankPhoneAndNeverSendsIdentityOverrides() = runBlocking {
        store.write(SessionCredential("synthetic-invalid-token", api.baseUrl))
        respond(200, profile)
        useCases.updateProfile("Synthetic Worker", "")
        val patch = server.takeRequest()
        assertEquals("PATCH", patch.method)
        assertEquals(setOf("fullName"), JsonParser.parseString(patch.body.readUtf8()).asJsonObject.keySet())
    }

    @Test fun invalidResponseDoesNotPersistAndRedirectDoesNotLeakToken() = runBlocking {
        respond(200, """{"id":1,"username":"synthetic@example.test","token":"synthetic-invalid-token"}""")
        respond(200, "{}")
        respond(204)
        fails(IdentityFailure.INVALID_RESPONSE) { useCases.login("synthetic@example.test", "SyntheticPass12") }
        assertNull(store.read())
        store.write(SessionCredential("synthetic-invalid-token", api.baseUrl))
        server.enqueue(MockResponse().setResponseCode(302).addHeader("Location", "http://localhost:1/"))
        fails(IdentityFailure.SERVER) { useCases.profile() }
        assertNotNull(store.read())
    }

    @Test fun credentialHostIsBoundAndReleaseDisallowsCleartext() = runBlocking {
        store.write(SessionCredential("synthetic-invalid-token", "https://different.example.test/"))
        fails(IdentityFailure.SESSION_INVALID) { useCases.profile() }
        assertEquals(0, server.requestCount)
        assertEquals("", ApiClient(server.url("/").toString(), false, store).baseUrl)
        assertEquals("", ApiClient("http://192.168.1.1/", true, store).baseUrl)
        assertEquals("", ApiClient("https://user:password@example.test/", false, store).baseUrl)
    }

    private suspend fun fails(expected: IdentityFailure, action: suspend () -> Any?) {
        try { action(); fail("Expected failure") } catch (e: IdentityException) { assertEquals(expected, e.reason) }
    }
}
