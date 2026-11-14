package com.nexorape.safework.iam

import com.nexorape.safework.core.network.ApiClient
import com.nexorape.safework.iam.application.*
import com.nexorape.safework.iam.domain.model.*
import com.nexorape.safework.iam.infrastructure.http.*
import java.util.UUID

/** Synthetic accounts only; never accepts a public destination or existing credentials. */
internal class LocalAccount(url: String) {
    init { require(Regex("http://127\\.0\\.0\\.1:(18086|18090)/").matches(url)) }
    val store = MemorySessions()
    val api = ApiClient(url, true, store)
    val iam = IdentityUseCases(HttpIdentityRepository(api, store))
    val companies = CompanyUseCases(HttpCompanyRepository(api))
    val email = UUID.randomUUID().toString() + "@example.test"
    private val password = "Synthetic-" + UUID.randomUUID()
    lateinit var user: UserProfile
    suspend fun register(): LocalAccount {
        iam.register("Synthetic Mobile Account", email, password)
        signIn()
        return this
    }
    suspend fun signIn() { user = iam.login(email, password) }
    suspend fun createCompany(): LocalAccount {
        companies.create("Synthetic Mobile " + UUID.randomUUID(), user)
        iam.logout()
        signIn()
        return this
    }
    suspend fun join(employer: LocalAccount): LocalAccount {
        val request = companies.request(requireNotNull(employer.user.companyId), user)
        employer.companies.decide(request, true, employer.user)
        iam.logout()
        signIn()
        return this
    }
}
