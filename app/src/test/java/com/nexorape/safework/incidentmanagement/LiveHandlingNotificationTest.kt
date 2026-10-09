package com.nexorape.safework.incidentmanagement

import com.google.gson.Gson
import com.nexorape.safework.core.network.*
import com.nexorape.safework.iam.MemorySessions
import com.nexorape.safework.iam.application.IdentityUseCases
import com.nexorape.safework.iam.domain.model.UserProfile
import com.nexorape.safework.iam.infrastructure.http.HttpIdentityRepository
import com.nexorape.safework.incidentmanagement.application.*
import com.nexorape.safework.incidentmanagement.domain.model.*
import com.nexorape.safework.incidentmanagement.infrastructure.http.*
import com.nexorape.safework.notificationmanagement.application.GetUserNotifications
import com.nexorape.safework.notificationmanagement.infrastructure.http.HttpNotificationRepository
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.util.Collections
import java.util.UUID
import java.util.concurrent.TimeUnit

class LiveHandlingNotificationTest {
    @Test fun responsibleTransitionsForeignCompanyAndRecipientIsolation() = runBlocking {
        val url = System.getenv("SAFEWORK_LIVE_API_URL")
        assumeTrue("Explicit synthetic local server required", !url.isNullOrBlank()); require(url!!.startsWith("http://127.0.0.1:"))
        val evidence = Collections.synchronizedList(mutableListOf<Map<String, Any>>())
        val client = OkHttpClient.Builder().followRedirects(false).followSslRedirects(false).retryOnConnectionFailure(false)
            .callTimeout(30, TimeUnit.SECONDS).addInterceptor { chain ->
                chain.proceed(chain.request()).also { evidence.add(mapOf("method" to chain.request().method,
                    "path" to chain.request().url.encodedPath, "status" to it.code)) }
            }.build()
        fun api(store: MemorySessions) = ApiClient(url, true, store, client)
        val gson = Gson(); val adminStore = MemorySessions(); val adminApi = api(adminStore)
        val operator = IdentityUseCases(HttpIdentityRepository(adminApi, adminStore))
        val admin = operator.login(System.getenv("SAFEWORK_TEST_ADMIN_EMAIL") ?: error("Missing synthetic operator email"),
            System.getenv("SAFEWORK_TEST_ADMIN_PASSWORD") ?: error("Missing synthetic operator credential"))
        val password = "Synthetic-" + UUID.randomUUID()
        data class Member(val api: ApiClient, val iam: IdentityUseCases, val user: UserProfile)
        suspend fun member(company: Long, employer: Boolean): Member {
            val email = UUID.randomUUID().toString() + "@example.test"
            val proof = adminApi.request("POST", "api/v1/companies/$company/invitations", gson.toJson(mapOf("emailAddress" to email)), expected = 201)!!.asJsonObject["invitationToken"].asString
            val store = MemorySessions(); val clientApi = api(store); val iam = IdentityUseCases(HttpIdentityRepository(clientApi, store))
            val registered = iam.register("Synthetic Same Display Name", email, password, proof)
            if (employer) adminApi.request("PATCH", "api/v1/administration/users/${registered.id.value}/roles", gson.toJson(mapOf("roles" to listOf("WORKER", "EMPLOYER"))))
            return Member(clientApi, iam, iam.login(email, password))
        }
        val worker = member(admin.companyId.value, false); val responsible = member(admin.companyId.value, true)
        val peer = member(admin.companyId.value, true)
        val otherCompany = adminApi.request("POST", "api/v1/companies", gson.toJson(mapOf("name" to "Synthetic Handling " + UUID.randomUUID())), expected = 201)!!.asJsonObject["id"].asLong
        val foreign = member(otherCompany, true)
        val queries = IncidentUseCases(HttpIncidentRepository(worker.api))
        val handling = IncidentHandlingUseCases(HttpIncidentHandlingRepository(responsible.api))
        val incident = queries.report("Synthetic handling hazard", "Fictional obstruction", "Synthetic room", worker.user)
        val takeBody = gson.toJson(mapOf("incidentId" to incident.id.value))
        rejected(worker.api, "POST", "api/v1/assignments", 403, takeBody)
        val assignment = handling.take(incident, responsible.user)
        assertEquals(responsible.user.id, assignment.responsibleId)
        val assigned = queries.detail(incident.id, worker.user)
        rejected(peer.api, "POST", "api/v1/assignments", 409, takeBody)
        rejected(peer.api, "POST", "api/v1/incidents/${incident.id.value}/start", 403)
        rejected(worker.api, "POST", "api/v1/incidents/${incident.id.value}/start", 403)
        rejected(foreign.api, "GET", "api/v1/incidents/${incident.id.value}", 404)
        rejected(foreign.api, "POST", "api/v1/incidents/${incident.id.value}/start", 404)
        rejected(foreign.api, "POST", "api/v1/incidents/${incident.id.value}/close", 404)
        rejected(responsible.api, "POST", "api/v1/incidents/${incident.id.value}/close", 409)
        assertEquals(IncidentStatus.ASSIGNED, queries.detail(incident.id, worker.user).status)
        val owned = handling.ownAssignments(responsible.user).single { it.incidentId == incident.id }
        assertNull(owned.completionDate)
        val started = handling.start(assigned, owned, responsible.user)
        assertEquals(IncidentStatus.IN_PROGRESS, started.status)
        rejected(peer.api, "POST", "api/v1/incidents/${incident.id.value}/close", 403)
        val inProgress = handling.ownAssignments(responsible.user).single { it.incidentId == incident.id }
        val closed = handling.close(started, inProgress, responsible.user)
        assertEquals(IncidentStatus.CLOSED, closed.status)
        val completed = handling.assignment(assignment.id, responsible.user)
        assertNotNull(completed.completionDate); assertEquals(IncidentStatus.CLOSED, completed.status)
        rejected(responsible.api, "POST", "api/v1/incidents/${incident.id.value}/close", 409)
        suspend fun notifications(member: Member) = GetUserNotifications(HttpNotificationRepository(member.api)).query(member.user)
        val workerNotifications = notifications(worker); val responsibleNotifications = notifications(responsible)
        // Retained event policy: creation to reporter; take/start/close to acting responsible.
        assertEquals(1, workerNotifications.size); assertEquals(3, responsibleNotifications.size)
        assertTrue(workerNotifications.map { it.id }.intersect(responsibleNotifications.map { it.id }.toSet()).isEmpty())
        assertTrue(notifications(peer).isEmpty()); assertTrue(notifications(foreign).isEmpty())
        assertTrue((workerNotifications + responsibleNotifications).all { !it.isRead })
        assertEquals(IncidentStatus.CLOSED, queries.detail(incident.id, worker.user).status)
        listOf(worker, responsible, peer, foreign).forEach { it.iam.logout() }; operator.logout()
        println("Live Handling/Notification HTTP results (methods/paths/statuses only): " + gson.toJson(evidence))
    }
    private suspend fun rejected(api: ApiClient, method: String, path: String, expected: Int, body: String? = null) {
        try { api.request(method, path, body); fail("Server accepted forbidden/conflicting operation") }
        catch (e: ApiException) { assertEquals(expected, e.status) }
    }
}
