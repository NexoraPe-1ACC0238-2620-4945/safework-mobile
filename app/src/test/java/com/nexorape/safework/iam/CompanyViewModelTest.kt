package com.nexorape.safework.iam

import com.nexorape.safework.iam.application.CompanyUseCases
import com.nexorape.safework.iam.domain.model.*
import com.nexorape.safework.iam.domain.repositories.*
import com.nexorape.safework.iam.presentation.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CompanyViewModelTest {
    private val user = UserProfile(UserId(1), null, FullName.of("Synthetic"), EmailAddress.of("synthetic@example.test"), null, setOf(Role.WORKER))
    private val company = CompanySummary(CompanyId(2), CompanyName.of("Synthetic Company"))
    private val request = MembershipRequest(1, company.id, user.id, user.fullName, user.email, RequestStatus.PENDING)
    @Test fun invalidationDiscardsLateDataAndDisablesOldViewModel() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val delayed = CompletableDeferred<List<MembershipRequest>>()
            var calls = 0
            val repo = object : FakeCompanyRepository() {
                override suspend fun ownRequests(page: Int): List<MembershipRequest> { calls++; return delayed.await() }
            }
            val events = MutableStateFlow(0)
            val vm = CompanyViewModel(CompanyUseCases(repo), user, events)
            runCurrent(); events.value++; runCurrent()
            delayed.complete(listOf(request)); runCurrent()
            vm.refresh(); runCurrent()
            assertEquals(1, calls)
            assertTrue(vm.state.value.requests.isEmpty())
            assertEquals(IdentityFailure.SESSION_INVALID, vm.state.value.error)
        } finally { Dispatchers.resetMain() }
    }
    @Test fun successfulCreationKeepsReauthenticationVisibleAndCannotBeRepeated() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            var creates = 0
            val repo = object : FakeCompanyRepository() {
                override suspend fun create(name: CompanyName): CompanySummary { creates++; return company }
            }
            val vm = CompanyViewModel(CompanyUseCases(repo), user, MutableStateFlow(0))
            runCurrent(); vm.create("Synthetic Company"); runCurrent()
            vm.create("Other Company"); vm.refresh(); runCurrent()
            assertEquals(1, creates)
            assertEquals(company, vm.state.value.created)
        } finally { Dispatchers.resetMain() }
    }
    @Test fun sendingARequestUpdatesPendingStateWithoutGrantingMembership() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val vm = CompanyViewModel(CompanyUseCases(object : FakeCompanyRepository() {
                override suspend fun request(company: CompanyId) = request
            }), user, MutableStateFlow(0))
            runCurrent(); vm.request(company); runCurrent()
            assertEquals(RequestStatus.PENDING, vm.state.value.requests.single().status)
            assertEquals(CompanyNotice.REQUEST_SENT, vm.state.value.notice)
            assertFalse(user.canAccessCompany)
        } finally { Dispatchers.resetMain() }
    }
    private open inner class FakeCompanyRepository : CompanyRepository {
        override suspend fun search(query: String, page: Int) = listOf(company)
        override suspend fun company(id: CompanyId) = company
        override suspend fun create(name: CompanyName) = company
        override suspend fun request(company: CompanyId) = request
        override suspend fun ownRequests(page: Int) = emptyList<MembershipRequest>()
        override suspend fun companyRequests(company: CompanyId, page: Int) = emptyList<MembershipRequest>()
        override suspend fun decide(company: CompanyId, requestId: Long, accept: Boolean) = request
        override suspend fun members(company: CompanyId, page: Int) = emptyList<UserProfile>()
        override suspend fun revoke(company: CompanyId, user: UserId) = error("Not invoked")
    }
}
