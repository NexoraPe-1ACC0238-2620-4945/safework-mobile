package com.nexorape.safework.incidentmanagement

import com.google.gson.Gson
import com.nexorape.safework.core.network.*
import com.nexorape.safework.iam.MemorySessions
import com.nexorape.safework.iam.application.IdentityUseCases
import com.nexorape.safework.iam.infrastructure.http.HttpIdentityRepository
import com.nexorape.safework.incidentmanagement.application.IncidentUseCases
import com.nexorape.safework.incidentmanagement.domain.model.*
import com.nexorape.safework.incidentmanagement.domain.repositories.*
import com.nexorape.safework.incidentmanagement.infrastructure.http.HttpIncidentRepository
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.util.Collections
import java.util.UUID
import java.util.concurrent.TimeUnit

class LiveIncidentTest {
    @Test fun syntheticReportQueriesAndForeignCompanyIsolation() = runBlocking {
        val url = System.getenv("SAFEWORK_LIVE_API_URL")
        assumeTrue("Explicit local fixture required", !url.isNullOrBlank()); require(url!!.startsWith("http://127.0.0.1:"))
        val evidence = Collections.synchronizedList(mutableListOf<Map<String, Any>>())
        val transport = OkHttpClient.Builder().followRedirects(false).followSslRedirects(false).retryOnConnectionFailure(false)
            .callTimeout(30, TimeUnit.SECONDS).addInterceptor { chain ->
                chain.proceed(chain.request()).also { evidence.add(mapOf("method" to chain.request().method,
                    "path" to chain.request().url.encodedPath, "status" to it.code)) }
            }.build()
        fun api(store: MemorySessions) = ApiClient(url, true, store, transport)
        val operatorStore = MemorySessions(); val operatorApi = api(operatorStore)
        val operator = IdentityUseCases(HttpIdentityRepository(operatorApi, operatorStore))
        val admin = operator.login(System.getenv("SAFEWORK_TEST_ADMIN_EMAIL"), System.getenv("SAFEWORK_TEST_ADMIN_PASSWORD"))
        val gson = Gson(); val password = "Synthetic-" + UUID.randomUUID().toString()
        suspend fun provision(company: Long): Triple<ApiClient, IdentityUseCases, com.nexorape.safework.iam.domain.model.UserProfile> {
            val email = UUID.randomUUID().toString() + "@example.test"
            val proof = operatorApi.request("POST", "api/v1/companies/$company/invitations", gson.toJson(mapOf("emailAddress" to email)), expected = 201)!!.asJsonObject["invitationToken"].asString
            val store = MemorySessions(); val client = api(store); val identity = IdentityUseCases(HttpIdentityRepository(client, store))
            identity.register("Synthetic Incident Worker", email, password, proof)
            return Triple(client, identity, identity.login(email, password))
        }
        val own = provision(admin.companyId.value)
        val incidents = IncidentUseCases(HttpIncidentRepository(own.first))
        incidents.list(own.third)
        val created = incidents.report("Synthetic mobile obstruction", "Fictional test obstruction", "Synthetic gate; -12.000000, -77.000000", own.third)
        assertEquals(own.third.id, created.reporterId); assertEquals(IncidentStatus.OPEN, created.status)
        assertEquals(created, incidents.detail(created.id, own.third)); assertNull(created.assigneeName)
        assertTrue(incidents.list(own.third).any { it.id == created.id })
        val company = operatorApi.request("POST", "api/v1/companies", gson.toJson(mapOf("name" to "Synthetic Mobile " + UUID.randomUUID())), expected = 201)!!.asJsonObject["id"].asLong
        val other = provision(company); val foreign = IncidentUseCases(HttpIncidentRepository(other.first))
        assertFalse(foreign.list(other.third).any { it.id == created.id })
        try { foreign.detail(created.id, other.third); fail("Foreign company detail accepted") }
        catch (e: IncidentException) { assertEquals(IncidentFailure.NOT_FOUND, e.reason) }
        own.second.logout(); other.second.logout(); operator.logout()
        println("Live Incident HTTP results (methods/paths/statuses only): " + gson.toJson(evidence))
    }
}
