package com.nexorape.safework.iam

import com.nexorape.safework.core.network.ApiException
import com.nexorape.safework.iam.domain.model.*
import com.nexorape.safework.iam.domain.repositories.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test

class LiveIdentityTest {
    @Test fun selfServiceCreationApprovalIsolationAndRevocation() = runBlocking {
        val url = System.getenv("SAFEWORK_LIVE_API_URL")
        assumeTrue("Explicit isolated local backend required", !url.isNullOrBlank())
        val employer = LocalAccount(url!!).register()
        assertNull(employer.user.companyId)
        assertFalse(employer.user.canAccessCompany)
        rejectStatus(403) { employer.api.request("GET", "api/v1/incidents") }
        employer.createCompany()
        assertTrue(employer.user.isEmployer)
        val worker = LocalAccount(url).register()
        val company = requireNotNull(employer.user.companyId)
        assertTrue(worker.companies.search("Synthetic Mobile", 0).all { it.name.value.isNotBlank() })
        val pending = worker.companies.request(company, worker.user)
        assertEquals(RequestStatus.PENDING, pending.status)
        expect(IdentityFailure.CONFLICT) { worker.companies.request(company, worker.user) }
        rejectStatus(403) { worker.api.request("GET", "api/v1/notifications/my-notifications") }
        assertEquals(RequestStatus.REJECTED, employer.companies.decide(pending, false, employer.user).status)
        val retry = worker.companies.request(company, worker.user)
        employer.companies.decide(retry, true, employer.user)
        expect(IdentityFailure.SESSION_INVALID) { worker.iam.profile() }
        assertNull(worker.store.read())
        worker.signIn()
        assertTrue(worker.user.hasActiveMembership)
        assertEquals(company, worker.user.companyId)
        val updated = worker.iam.updateProfile("Synthetic Mobile Edited", "+51 900 000 000")
        assertEquals(updated, worker.iam.restore())
        assertTrue(employer.companies.members(employer.user).any { it.id == worker.user.id })
        employer.companies.revoke(updated, employer.user)
        expect(IdentityFailure.SESSION_INVALID) { worker.iam.profile() }
        worker.signIn()
        assertEquals(MembershipStatus.REVOKED, worker.user.membershipStatus)
        rejectStatus(403) { worker.api.request("GET", "api/v1/incidents") }
        worker.companies.request(company, worker.user)
        val foreign = LocalAccount(url).register().createCompany()
        rejectStatus(404) { foreign.api.request("GET", "api/v1/companies/${company.value}/membership-requests") }
        listOf(worker, employer, foreign).forEach { it.iam.logout() }
        println("LIVE_SELF_SERVICE_IAM_PASSED (synthetic local accounts only)")
    }
    private suspend fun expect(reason: IdentityFailure, action: suspend () -> Any?) {
        try { action(); fail("Expected rejection") } catch (e: IdentityException) { assertEquals(reason, e.reason) }
    }
    private suspend fun rejectStatus(status: Int, action: suspend () -> Any?) {
        try { action(); fail("Expected HTTP rejection") } catch (e: ApiException) { assertEquals(status, e.status) }
    }
}
