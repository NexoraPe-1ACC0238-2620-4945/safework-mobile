package com.nexorape.safework.iam.domain.repositories

import com.nexorape.safework.iam.domain.model.*

interface CompanyRepository {
    suspend fun search(query: String, page: Int): List<CompanySummary>
    suspend fun company(id: CompanyId): CompanySummary
    suspend fun create(name: CompanyName): CompanySummary
    suspend fun request(company: CompanyId): MembershipRequest
    suspend fun ownRequests(page: Int): List<MembershipRequest>
    suspend fun companyRequests(company: CompanyId, page: Int): List<MembershipRequest>
    suspend fun decide(company: CompanyId, requestId: Long, accept: Boolean): MembershipRequest
    suspend fun members(company: CompanyId, page: Int): List<UserProfile>
    suspend fun revoke(company: CompanyId, user: UserId): UserProfile
}
