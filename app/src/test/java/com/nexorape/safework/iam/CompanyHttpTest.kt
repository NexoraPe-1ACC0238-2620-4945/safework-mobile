package com.nexorape.safework.iam

import com.google.gson.JsonParser
import com.nexorape.safework.core.network.*
import com.nexorape.safework.iam.application.CompanyUseCases
import com.nexorape.safework.iam.domain.model.*
import com.nexorape.safework.iam.domain.repositories.*
import com.nexorape.safework.iam.infrastructure.http.HttpCompanyRepository
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CompanyHttpTest {
    private lateinit var server: MockWebServer
    private lateinit var cases: CompanyUseCases
    private val actor = UserProfile(UserId(1), null, FullName.of("Synthetic Worker"), EmailAddress.of("worker@example.test"), null, setOf(Role.WORKER))
    private val employer = actor.copy(companyId = CompanyId(2), roles = setOf(Role.EMPLOYER), membershipStatus = MembershipStatus.ACTIVE)
    private val pending = """{"id":7,"companyId":2,"userId":1,"fullName":"Synthetic Worker","email":"worker@example.test","status":"PENDING"}"""
    @Before fun setup() {
        server = MockWebServer().apply { start(java.net.InetAddress.getByName("127.0.0.1"), 0) }
        val store = MemorySessions()
        val api = ApiClient("http://127.0.0.1:${server.port}/", true, store)
        store.write(SessionCredential("synthetic-invalid-token", api.baseUrl))
        cases = CompanyUseCases(HttpCompanyRepository(api))
    }
    @After fun close() { server.shutdown() }
    private fun response(code: Int, body: String) { server.enqueue(MockResponse().setResponseCode(code).setBody(body)) }

    @Test fun discoveryEncodesSearchAsDataAndKeepsPagination() = runBlocking {
        response(200, """[{"id":2,"name":"Synthetic & Team"}]""")
        val result = cases.search("Synthetic & Team?", 1)
        val request = server.takeRequest()
        assertEquals("/api/v1/companies", request.requestUrl!!.encodedPath)
        assertEquals("Synthetic & Team?", request.requestUrl!!.queryParameter("search"))
        assertEquals("1", request.requestUrl!!.queryParameter("page"))
        assertEquals("20", request.requestUrl!!.queryParameter("size"))
        assertEquals("Synthetic & Team", result.single().name.value)
    }
    @Test fun creationSendsOnlyNameAndRequestHasNoBody() = runBlocking {
        response(201, """{"id":2,"name":"Synthetic"}""")
        val company = cases.create("Synthetic", actor)
        val create = server.takeRequest()
        assertEquals(setOf("name"), JsonParser.parseString(create.body.readUtf8()).asJsonObject.keySet())
        response(201, pending)
        cases.request(company.id, actor)
        val request = server.takeRequest()
        assertEquals("/api/v1/companies/2/membership-requests", request.path)
        assertEquals("POST", request.method)
        assertEquals(0L, request.bodySize)
    }
    @Test fun duplicateRequestIsAConflictAndForeignApplicantDataIsRejected() = runBlocking {
        response(409, "{}")
        expect(IdentityFailure.CONFLICT) { cases.request(CompanyId(2), actor) }
        response(200, "[${pending.replace("\"userId\":1", "\"userId\":99")}]")
        expect(IdentityFailure.INVALID_RESPONSE) { cases.ownRequests(actor, 0) }
    }
    @Test fun decisionsAreCompanyScopedAndUseEmptyBody() = runBlocking {
        response(200, "[$pending]")
        val request = cases.requests(employer, 0).single()
        server.takeRequest()
        response(200, pending.replace("PENDING", "ACCEPTED"))
        assertEquals(RequestStatus.ACCEPTED, cases.decide(request, true, employer).status)
        val wire = server.takeRequest()
        assertEquals("/api/v1/companies/2/membership-requests/7/accept", wire.path)
        assertEquals(0L, wire.bodySize)
    }
    @Test fun revocationUsesDeleteAndChecksReturnedMembership() = runBlocking {
        val member = actor.copy(id = UserId(3), companyId = CompanyId(2), membershipStatus = MembershipStatus.ACTIVE)
        response(200, """{"id":3,"companyId":2,"fullName":"Synthetic Worker","email":"worker@example.test","roles":["WORKER"],"membershipStatus":"REVOKED"}""")
        assertEquals(MembershipStatus.REVOKED, cases.revoke(member, employer).membershipStatus)
        val request = server.takeRequest()
        assertEquals("DELETE", request.method)
        assertEquals("/api/v1/companies/2/members/3", request.path)
    }
    @Test fun revokedSessionDoesNotRetainCompanyData() = runBlocking {
        response(401, "{}")
        expect(IdentityFailure.SESSION_INVALID) { cases.requests(employer, 0) }
    }
    private suspend fun expect(reason: IdentityFailure, action: suspend () -> Any?) {
        try { action(); fail("Expected rejection") } catch (e: IdentityException) { assertEquals(reason, e.reason) }
    }
}
