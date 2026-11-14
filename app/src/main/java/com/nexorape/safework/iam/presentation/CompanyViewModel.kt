package com.nexorape.safework.iam.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexorape.safework.iam.application.CompanyUseCases
import com.nexorape.safework.iam.domain.model.*
import com.nexorape.safework.iam.domain.repositories.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class CompanyNotice { REQUEST_SENT, ACCEPTED, REJECTED, REVOKED }
data class CompanyUiState(
    val busy: Boolean = false,
    val loaded: Boolean = false,
    val error: IdentityFailure? = null,
    val notice: CompanyNotice? = null,
    val company: CompanySummary? = null,
    val created: CompanySummary? = null,
    val companies: List<CompanySummary> = emptyList(),
    val requests: List<MembershipRequest> = emptyList(),
    val members: List<UserProfile> = emptyList(),
    val search: String = "",
    val page: Int = 0,
    val requestPage: Int = 0,
    val memberPage: Int = 0,
    val moreCompanies: Boolean = false,
    val moreRequests: Boolean = false,
    val moreMembers: Boolean = false,
)

class CompanyViewModel(private val cases: CompanyUseCases, private val actor: UserProfile,
                       invalidations: StateFlow<Int>) : ViewModel() {
    private val mutableState = MutableStateFlow(CompanyUiState())
    val state = mutableState.asStateFlow()
    private var generation = 0
    private var active = true
    init {
        viewModelScope.launch {
            invalidations.drop(1).collect {
                generation++; active = false
                mutableState.value = CompanyUiState(error = IdentityFailure.SESSION_INVALID)
            }
        }
        refresh()
    }
    fun refresh() = perform {
        val requests = if (actor.isEmployer) cases.requests(actor, 0) else cases.ownRequests(actor, 0)
        val companyId = actor.companyId ?: requests.firstOrNull { it.status == RequestStatus.PENDING }?.companyId
        val company = companyId?.let { id ->
            try { cases.company(id) } catch (e: IdentityException) {
                if (e.reason != IdentityFailure.NOT_FOUND) throw e
                null
            }
        }
        val members = if (actor.isEmployer) cases.members(actor) else emptyList()
        val companies = if (actor.companyId == null) cases.search(state.value.search, 0) else emptyList()
        state.value.copy(loaded = true, company = company, requests = requests, members = members,
            companies = companies, page = 0, requestPage = 0, memberPage = 0, moreCompanies = companies.size == 20,
            moreRequests = requests.size == 20, moreMembers = members.size == 20)
    }
    fun search(query: String, next: Boolean = false) = perform {
        val page = if (next) state.value.page + 1 else 0
        val found = cases.search(query, page)
        state.value.copy(search = query, page = page, companies = if (next) (state.value.companies + found).distinctBy { it.id } else found,
            moreCompanies = found.size == 20 && page < 10000)
    }
    fun moreRequests() = perform {
        val page = state.value.requestPage + 1
        val found = if (actor.isEmployer) cases.requests(actor, page) else cases.ownRequests(actor, page)
        state.value.copy(requestPage = page, requests = (state.value.requests + found).distinctBy { it.id },
            moreRequests = found.size == 20 && page < 10000)
    }
    fun create(name: String) = perform {
        val company = cases.create(name, actor)
        // Creation revokes the current session. Keep this result visible until the user signs in again.
        state.value.copy(created = company, companies = emptyList(), requests = emptyList(), moreCompanies = false, moreRequests = false)
    }
    fun moreMembers() = perform {
        val page = state.value.memberPage + 1
        val found = cases.members(actor, page)
        state.value.copy(memberPage = page, members = (state.value.members + found).distinctBy { it.id },
            moreMembers = found.size == 20 && page < 10000)
    }
    fun request(company: CompanySummary) = perform {
        val requested = cases.request(company.id, actor)
        state.value.copy(requests = listOf(requested) + state.value.requests.filterNot { it.id == requested.id }, notice = CompanyNotice.REQUEST_SENT)
    }
    fun decide(request: MembershipRequest, accept: Boolean) = perform {
        val decided = cases.decide(request, accept, actor)
        val updated = state.value.copy(requests = state.value.requests.map { if (it.id == decided.id) decided else it },
            notice = if (accept) CompanyNotice.ACCEPTED else CompanyNotice.REJECTED)
        // Preserve a successful decision even if refreshing members subsequently fails.
        try {
            val members = cases.members(actor)
            updated.copy(members = members, memberPage = 0, moreMembers = members.size == 20)
        }
        catch (e: IdentityException) {
            if (e.reason == IdentityFailure.SESSION_INVALID) throw e
            updated.copy(error = e.reason)
        }
    }
    fun revoke(member: UserProfile) = perform {
        val revoked = cases.revoke(member, actor)
        state.value.copy(members = state.value.members.map { if (it.id == revoked.id) revoked else it }, notice = CompanyNotice.REVOKED)
    }
    private fun perform(action: suspend () -> CompanyUiState) {
        if (!active || state.value.busy || state.value.created != null) return
        val revision = generation
        mutableState.value = state.value.copy(busy = true, error = null, notice = null)
        viewModelScope.launch {
            try { val result = action(); if (revision == generation) mutableState.value = result.copy(busy = false) }
            catch (e: CancellationException) { throw e }
            catch (e: IdentityException) {
                if (revision == generation) mutableState.value = state.value.copy(busy = false, error = e.reason)
            } finally { if (revision == generation) mutableState.value = state.value.copy(busy = false) }
        }
    }
}
