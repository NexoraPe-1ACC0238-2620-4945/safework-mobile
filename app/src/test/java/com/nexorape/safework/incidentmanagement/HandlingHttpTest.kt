package com.nexorape.safework.incidentmanagement

import com.google.gson.JsonParser
import com.nexorape.safework.core.network.*
import com.nexorape.safework.iam.MemorySessions
import com.nexorape.safework.iam.domain.model.*
import com.nexorape.safework.incidentmanagement.application.*
import com.nexorape.safework.incidentmanagement.domain.model.*
import com.nexorape.safework.incidentmanagement.domain.repositories.*
import com.nexorape.safework.incidentmanagement.infrastructure.http.*
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.*
import org.junit.*
import org.junit.Assert.*
import java.time.Instant

class HandlingHttpTest {
    private lateinit var server: MockWebServer
    private lateinit var cases: IncidentHandlingUseCases
    private val actor = UserProfile(UserId(3), CompanyId(2), FullName.of("Synthetic Employer"), EmailAddress.of("employer@example.test"), null, setOf(Role.EMPLOYER))
    private val incident = Incident(IncidentId(5), UserId(1), CompanyId(2), IncidentTitle.of("Synthetic"), IncidentDescription.of("Synthetic"), IncidentLocation.of("Manual"), IncidentStatus.OPEN, null, "Synthetic", null)
    private val assignment = """{"id":9,"incidentId":5,"userId":3,"incidentTitle":"Synthetic","status":"ASSIGNED","assignedAt":"2026-10-08T13:00:00+01:00","priority":"MEDIUM","completionDate":null}"""
    private fun response(status: String) = """{"id":5,"userId":1,"companyId":2,"title":"Synthetic","description":"Synthetic","location":"Manual","status":"$status","documentUrl":null,"reporterName":"Synthetic","assigneeName":"Synthetic Employer"}"""
    @Before
    fun setup() {
        server = MockWebServer().apply {
            start(java.net.InetAddress.getByName("127.0.0.1"), 0)
        }

        val store = MemorySessions()

        val api = ApiClient(
            "http://127.0.0.1:${server.port}/",
            true,
            store
        )

        store.write(
            SessionCredential("synthetic-invalid-token", api.baseUrl)
        )

        cases = IncidentHandlingUseCases(
            HttpIncidentHandlingRepository(api)
        )
    }
    @After fun close() { server.shutdown() }
    @Test fun takeAndTransitionsUseOnlyIncidentIdAndNoActionPayload() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(201).setBody(assignment))
        server.enqueue(MockResponse().setBody(response("IN_PROGRESS")))
        server.enqueue(MockResponse().setBody(response("CLOSED")))
        val taken = cases.take(incident, actor)
        assertEquals(actor.id, taken.responsibleId); assertEquals(Instant.parse("2026-10-08T12:00:00Z"), taken.assignedAt)
        val started = cases.start(incident.copy(status = IncidentStatus.ASSIGNED), taken, actor)
        cases.close(started, taken.copy(status = IncidentStatus.IN_PROGRESS), actor)
        val request = server.takeRequest()
        assertEquals(setOf("incidentId"), JsonParser.parseString(request.body.readUtf8()).asJsonObject.keySet())
        assertEquals("/api/v1/assignments", request.path)
        assertEquals(0L, server.takeRequest().bodySize); assertEquals(0L, server.takeRequest().bodySize)
    }
    @Test fun localRoleCompanyResponsibleAndStateRulesBlockInvalidWrites() = runBlocking {
        expect(IncidentFailure.FORBIDDEN) { cases.take(incident, actor.copy(roles = setOf(Role.WORKER))) }
        expect(IncidentFailure.NOT_FOUND) { cases.take(incident.copy(companyId = CompanyId(4)), actor) }
        expect(IncidentFailure.STATE_CONFLICT) { cases.take(incident.copy(status = IncidentStatus.CLOSED), actor) }
        val other = Assignment(AssignmentId(9), incident.id, UserId(4), "Synthetic", IncidentStatus.ASSIGNED, Instant.EPOCH, AssignmentPriority.LOW, null)
        expect(IncidentFailure.FORBIDDEN) { cases.start(incident.copy(status = IncidentStatus.ASSIGNED), other, actor) }
        assertEquals(0, server.requestCount)
    }
    @Test fun wrongResponsibleResponseAndServerConflictAreRejected() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(201).setBody(assignment.replace("\"userId\":3", "\"userId\":4")))
        expect(IncidentFailure.INVALID_RESPONSE) { cases.take(incident, actor) }
        server.enqueue(MockResponse().setResponseCode(409))
        expect(IncidentFailure.STATE_CONFLICT) { cases.take(incident, actor) }
    }
    private suspend fun expect(reason: IncidentFailure, action: suspend () -> Any?) {
        try { action(); fail("Expected rejection") } catch (e: IncidentException) { assertEquals(reason, e.reason) }
    }
}
