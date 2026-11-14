package com.nexorape.safework.iam.application

import com.nexorape.safework.iam.domain.model.*
import com.nexorape.safework.iam.domain.repositories.*

class CompanyUseCases(private val repository: CompanyRepository) {
    suspend fun search(query: String, page: Int): List<CompanySummary> {
        validPage(page)
        if (query.length > 120) fail(IdentityFailure.INVALID_INPUT)
        return repository.search(query.trim(), page)
    }
    suspend fun company(id: CompanyId) = repository.company(id).also {
        if (it.id != id) fail(IdentityFailure.INVALID_RESPONSE)
    }
    suspend fun create(name: String, actor: UserProfile): CompanySummary {
        if (actor.companyId != null) fail(IdentityFailure.FORBIDDEN)
        val parsed = try { CompanyName.of(name) } catch (_: IllegalArgumentException) { fail(IdentityFailure.INVALID_INPUT) }
        return repository.create(parsed)
    }
    suspend fun request(company: CompanyId, actor: UserProfile): MembershipRequest {
        if (actor.hasActiveMembership || actor.companyId?.let { it != company } == true) fail(IdentityFailure.FORBIDDEN)
        return repository.request(company).also {
            if (it.companyId != company || it.userId != actor.id || it.status != RequestStatus.PENDING)
                fail(IdentityFailure.INVALID_RESPONSE)
        }
    }
    suspend fun ownRequests(actor: UserProfile, page: Int): List<MembershipRequest> {
        validPage(page)
        return repository.ownRequests(page).also { list ->
            if (list.any { it.userId != actor.id }) fail(IdentityFailure.INVALID_RESPONSE)
        }
    }
    suspend fun requests(actor: UserProfile, page: Int): List<MembershipRequest> {
        val company = employer(actor); validPage(page)
        return repository.companyRequests(company, page).also { list ->
            if (list.any { it.companyId != company }) fail(IdentityFailure.INVALID_RESPONSE)
        }
    }
    suspend fun decide(request: MembershipRequest, accept: Boolean, actor: UserProfile): MembershipRequest {
        val company = employer(actor)
        if (request.companyId != company) fail(IdentityFailure.NOT_FOUND)
        if (request.status != RequestStatus.PENDING) fail(IdentityFailure.CONFLICT)
        return repository.decide(company, request.id, accept).also {
            if (it.id != request.id || it.userId != request.userId || it.companyId != company ||
                it.status != if (accept) RequestStatus.ACCEPTED else RequestStatus.REJECTED) fail(IdentityFailure.INVALID_RESPONSE)
        }
    }
    suspend fun members(actor: UserProfile, page: Int = 0): List<UserProfile> {
        val company = employer(actor); validPage(page)
        return repository.members(company, page).also { list ->
            if (list.any { it.companyId != company }) fail(IdentityFailure.INVALID_RESPONSE)
        }
    }
    suspend fun revoke(member: UserProfile, actor: UserProfile): UserProfile {
        val company = employer(actor)
        if (member.id == actor.id || Role.EMPLOYER in member.roles) fail(IdentityFailure.FORBIDDEN)
        if (member.companyId != company) fail(IdentityFailure.NOT_FOUND)
        return repository.revoke(company, member.id).also {
            if (it.id != member.id || it.companyId != company || it.membershipStatus != MembershipStatus.REVOKED)
                fail(IdentityFailure.INVALID_RESPONSE)
        }
    }
    private fun employer(actor: UserProfile): CompanyId {
        if (!actor.isEmployer) fail(IdentityFailure.FORBIDDEN)
        return requireNotNull(actor.companyId)
    }
    private fun validPage(page: Int) { if (page !in 0..10000) fail(IdentityFailure.INVALID_INPUT) }
    private fun fail(reason: IdentityFailure): Nothing = throw IdentityException(reason)
}
