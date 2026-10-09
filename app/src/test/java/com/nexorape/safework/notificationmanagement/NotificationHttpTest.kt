package com.nexorape.safework.notificationmanagement

import com.nexorape.safework.core.network.*
import com.nexorape.safework.iam.MemorySessions
import com.nexorape.safework.iam.domain.model.*
import com.nexorape.safework.notificationmanagement.application.GetUserNotifications
import com.nexorape.safework.notificationmanagement.domain.repositories.*
import com.nexorape.safework.notificationmanagement.infrastructure.http.HttpNotificationRepository
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.Assert.*
import java.time.Instant

class NotificationHttpTest {
    private lateinit var server: MockWebServer
    private lateinit var store: MemorySessions
    private lateinit var query: GetUserNotifications
    private val actor = UserProfile(UserId(1), CompanyId(2), FullName.of("Synthetic"), EmailAddress.of("synthetic@example.test"), null, setOf(Role.WORKER))
    private val json = """{"id":"00000000-0000-0000-0000-000000000001","subject":"Synthetic notification","body":"Synthetic body","createdAt":"2026-10-08T13:00:00+01:00","isRead":false}"""
    @Before fun setup() {
        server = MockWebServer().apply {
            start(java.net.InetAddress.getByName("127.0.0.1"), 0)
        }; store = MemorySessions()
        val api = ApiClient(server.url("http://127.0.0.1:${server.port}/").toString(), true, store); store.write(SessionCredential("synthetic-invalid-token", api.baseUrl))
        query = GetUserNotifications(HttpNotificationRepository(api))
    }
    @After fun close() { server.shutdown() }
    @Test fun ownedQueryUsesFiveFieldsZonedDatesAndNoRecipientSelector() = runBlocking {
        server.enqueue(MockResponse().setBody("[$json]"))
        val notification = query.query(actor).single()
        assertEquals(Instant.parse("2026-10-08T12:00:00Z"), notification.createdAt); assertFalse(notification.isRead)
        val request = server.takeRequest()
        assertEquals("/api/v1/notifications/my-notifications", request.path); assertEquals(0L, request.bodySize)
        assertEquals("Bearer synthetic-invalid-token", request.getHeader("Authorization"))
    }
    @Test fun malformedResponseForbiddenAndRevocationAreDistinct() = runBlocking {
        server.enqueue(MockResponse().setBody("[${json.replace("00000000-0000-0000-0000-000000000001", "not-a-uuid")}]"))
        expect(NotificationFailure.INVALID_RESPONSE) { query.query(actor) }
        server.enqueue(MockResponse().setResponseCode(403))
        expect(NotificationFailure.FORBIDDEN) { query.query(actor) }; assertNotNull(store.read())
        server.enqueue(MockResponse().setResponseCode(401))
        expect(NotificationFailure.SESSION_INVALID) { query.query(actor) }; assertNull(store.read())
    }
    private suspend fun expect(reason: NotificationFailure, action: suspend () -> Any?) {
        try { action(); fail("Expected rejection") } catch (e: NotificationException) { assertEquals(reason, e.reason) }
    }
}
