package com.nexorape.safework.incidentmanagement

import com.nexorape.safework.core.network.*
import com.nexorape.safework.iam.LocalAccount
import com.nexorape.safework.incidentmanagement.application.*
import com.nexorape.safework.incidentmanagement.domain.model.*
import com.nexorape.safework.incidentmanagement.infrastructure.http.*
import com.nexorape.safework.notificationmanagement.application.GetUserNotifications
import com.nexorape.safework.notificationmanagement.infrastructure.http.HttpNotificationRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test

class LiveHandlingNotificationTest {
    @Test fun responsibleTransitionsForeignCompanyAndRecipientIsolation() = runBlocking {
        val url = System.getenv("SAFEWORK_LIVE_API_URL")
        assumeTrue("Explicit isolated local backend required", !url.isNullOrBlank())
        val employer = LocalAccount(url!!).register().createCompany()
        val worker = LocalAccount(url).register().join(employer)
        val peer = LocalAccount(url).register().join(employer)
        val foreign = LocalAccount(url).register().createCompany()
        val queries = IncidentUseCases(HttpIncidentRepository(worker.api))
        val handling = IncidentHandlingUseCases(HttpIncidentHandlingRepository(employer.api))
        val incident = queries.report("Synthetic handling hazard", "Fictional obstruction", "Synthetic room", worker.user)
        val assignment = handling.take(incident, employer.user)
        val assigned = queries.detail(incident.id, worker.user)
        rejected(peer.api, "POST", "api/v1/incidents/${incident.id.value}/start", 403)
        rejected(foreign.api, "GET", "api/v1/incidents/${incident.id.value}", 404)
        rejected(employer.api, "POST", "api/v1/incidents/${incident.id.value}/close", 409)
        val owned = handling.ownAssignments(employer.user).single { it.incidentId == incident.id }
        val started = handling.start(assigned, owned, employer.user)
        assertEquals(IncidentStatus.IN_PROGRESS, started.status)
        val inProgress = handling.ownAssignments(employer.user).single { it.incidentId == incident.id }
        val closed = handling.close(started, inProgress, employer.user)
        assertEquals(IncidentStatus.CLOSED, closed.status)
        assertNotNull(handling.assignment(assignment.id, employer.user).completionDate)
        rejected(employer.api, "POST", "api/v1/incidents/${incident.id.value}/close", 409)
        suspend fun notifications(member: LocalAccount) = GetUserNotifications(HttpNotificationRepository(member.api)).query(member.user)
        val reporterNotifications = notifications(worker)
        val responsibleNotifications = notifications(employer)
        assertEquals(1, reporterNotifications.size)
        assertEquals(3, responsibleNotifications.size)
        assertTrue(reporterNotifications.map { it.id }.intersect(responsibleNotifications.map { it.id }.toSet()).isEmpty())
        assertTrue(notifications(peer).isEmpty())
        assertTrue(notifications(foreign).isEmpty())
        listOf(worker, employer, peer, foreign).forEach { it.iam.logout() }
        println("LIVE_SELF_SERVICE_HANDLING_NOTIFICATIONS_PASSED")
    }
    private suspend fun rejected(api: ApiClient, method: String, path: String, expected: Int) {
        try { api.request(method, path); fail("Server accepted forbidden/conflicting operation") }
        catch (e: ApiException) { assertEquals(expected, e.status) }
    }
}
