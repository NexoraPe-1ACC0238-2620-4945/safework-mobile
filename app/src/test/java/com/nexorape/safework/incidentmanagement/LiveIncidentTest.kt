package com.nexorape.safework.incidentmanagement

import com.nexorape.safework.iam.LocalAccount
import com.nexorape.safework.incidentmanagement.application.IncidentUseCases
import com.nexorape.safework.incidentmanagement.domain.model.*
import com.nexorape.safework.incidentmanagement.domain.repositories.*
import com.nexorape.safework.incidentmanagement.infrastructure.http.HttpIncidentRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test

class LiveIncidentTest {
    @Test fun syntheticReportQueriesAndForeignCompanyIsolation() = runBlocking {
        val url = System.getenv("SAFEWORK_LIVE_API_URL")
        assumeTrue("Explicit isolated local backend required", !url.isNullOrBlank())
        val employer = LocalAccount(url!!).register().createCompany()
        val worker = LocalAccount(url).register().join(employer)
        val foreign = LocalAccount(url).register().createCompany()
        val incidents = IncidentUseCases(HttpIncidentRepository(worker.api))
        val created = incidents.report("Synthetic mobile obstruction", "Fictional test obstruction", "Synthetic gate", worker.user)
        assertEquals(worker.user.id, created.reporterId)
        assertEquals(IncidentStatus.OPEN, created.status)
        assertEquals(created, incidents.detail(created.id, worker.user))
        assertTrue(incidents.list(worker.user).any { it.id == created.id })
        val other = IncidentUseCases(HttpIncidentRepository(foreign.api))
        assertFalse(other.list(foreign.user).any { it.id == created.id })
        try { other.detail(created.id, foreign.user); fail("Foreign company detail accepted") }
        catch (e: IncidentException) { assertEquals(IncidentFailure.NOT_FOUND, e.reason) }
        listOf(worker, employer, foreign).forEach { it.iam.logout() }
        println("LIVE_SELF_SERVICE_INCIDENTS_PASSED")
    }
}
