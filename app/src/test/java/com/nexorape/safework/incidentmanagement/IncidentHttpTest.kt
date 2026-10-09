package com.nexorape.safework.incidentmanagement

import com.google.gson.JsonParser
import com.nexorape.safework.core.network.*
import com.nexorape.safework.iam.MemorySessions
import com.nexorape.safework.iam.domain.model.*
import com.nexorape.safework.incidentmanagement.application.IncidentUseCases
import com.nexorape.safework.incidentmanagement.domain.model.*
import com.nexorape.safework.incidentmanagement.domain.repositories.*
import com.nexorape.safework.incidentmanagement.infrastructure.http.HttpIncidentRepository
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.Assert.*

class IncidentHttpTest {
    private lateinit var server: MockWebServer
    private lateinit var cases: IncidentUseCases
    private lateinit var store: MemorySessions
    private val actor = UserProfile(UserId(1), CompanyId(2), FullName.of("Synthetic Worker"), EmailAddress.of("synthetic@example.test"), null, setOf(Role.WORKER))
    private val json = """{"id":5,"userId":1,"companyId":2,"title":"Synthetic hazard","description":"Synthetic description","location":"Manual location","status":"OPEN","documentUrl":null,"reporterName":"Synthetic Worker","assigneeName":null}"""
    @Before fun setup() {
        server = MockWebServer().apply { start() }; store = MemorySessions()
        val api = ApiClient(server.url("/").toString(), true, store)
        store.write(SessionCredential("synthetic-invalid-token", api.baseUrl))
        cases = IncidentUseCases(HttpIncidentRepository(api))
    }
    @After fun close() { server.shutdown() }
    @Test fun listDetailAndReportUsePublishedFields() = runBlocking {
        server.enqueue(MockResponse().setBody("[$json]")); server.enqueue(MockResponse().setBody(json))
        server.enqueue(MockResponse().setResponseCode(201).setBody(json))
        assertEquals(1, cases.list(actor).size)
        assertNull(cases.detail(IncidentId(5), actor).assigneeName)
        assertEquals(IncidentStatus.OPEN, cases.report("Synthetic hazard", "Synthetic description", "Manual location", actor).status)
        assertEquals("/api/v1/incidents", server.takeRequest().path)
        assertEquals("/api/v1/incidents/5", server.takeRequest().path)
        val request = server.takeRequest()
        assertEquals(setOf("title", "description", "location"), JsonParser.parseString(request.body.readUtf8()).asJsonObject.keySet())
        assertEquals("Bearer synthetic-invalid-token", request.getHeader("Authorization"))
    }
    @Test fun rejectsWrongCompanyAndInvalidInputBeforeReport() = runBlocking {
        server.enqueue(MockResponse().setBody(json.replace("\"companyId\":2", "\"companyId\":3")))
        expect(IncidentFailure.INVALID_RESPONSE) { cases.detail(IncidentId(5), actor) }
        expect(IncidentFailure.INVALID_INPUT) { cases.report(" ", "d", "l", actor) }
        assertEquals(1, server.requestCount)
    }
    @Test fun authErrorsAndInvisibleDetailAreDistinct() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(404))
        expect(IncidentFailure.NOT_FOUND) { cases.detail(IncidentId(5), actor) }
        server.enqueue(MockResponse().setResponseCode(403))
        expect(IncidentFailure.FORBIDDEN) { cases.list(actor) }; assertNotNull(store.read())
        server.enqueue(MockResponse().setResponseCode(401))
        expect(IncidentFailure.SESSION_INVALID) { cases.list(actor) }; assertNull(store.read())
    }
    private suspend fun expect(reason: IncidentFailure, action: suspend () -> Any?) {
        try { action(); fail("Expected rejection") } catch (e: IncidentException) { assertEquals(reason, e.reason) }
    }
}
