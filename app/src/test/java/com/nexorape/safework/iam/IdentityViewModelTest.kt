package com.nexorape.safework.iam

import com.nexorape.safework.iam.application.IdentityUseCases
import com.nexorape.safework.iam.domain.model.*
import com.nexorape.safework.iam.domain.repositories.*
import com.nexorape.safework.iam.presentation.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IdentityViewModelTest {
    private val user = UserProfile(UserId(1), CompanyId(2), FullName.of("Synthetic Worker"),
        EmailAddress.of("synthetic@example.test"), null, setOf(Role.WORKER))

    @Test fun revocationCannotBeOverwrittenByLateProfileResponse() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val events = MutableStateFlow(0)
            val delayed = CompletableDeferred<UserProfile>()
            val repo = object : FakeRepository() { override suspend fun profile() = delayed.await() }
            val vm = IdentityViewModel(IdentityUseCases(repo), events)
            runCurrent()
            assertEquals(IdentityScreen.PROFILE, vm.state.value.screen)
            vm.refresh(); runCurrent()
            events.value++; runCurrent()
            delayed.complete(user); runCurrent()
            assertEquals(IdentityScreen.LOGIN, vm.state.value.screen)
            assertNull(vm.state.value.user)
            assertEquals(IdentityFailure.SESSION_INVALID, vm.state.value.error)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun logoutFailureKeepsProfileAndSuccessfulLogoutHasNoExpiredNotice() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val events = MutableStateFlow(0)
            var failLogout = true
            val repo = object : FakeRepository() {
                override suspend fun logout() {
                    if (failLogout) throw IdentityException(IdentityFailure.NETWORK)
                    events.value++
                    yield()
                }
            }
            val vm = IdentityViewModel(IdentityUseCases(repo), events)
            runCurrent(); vm.logout(); runCurrent()
            assertEquals(IdentityScreen.PROFILE, vm.state.value.screen)
            assertEquals(IdentityFailure.NETWORK, vm.state.value.error)
            failLogout = false; vm.logout(); runCurrent()
            assertEquals(IdentityScreen.LOGIN, vm.state.value.screen)
            assertNull(vm.state.value.error)
        } finally { Dispatchers.resetMain() }
    }

    private open inner class FakeRepository : IdentityRepository {
        override suspend fun restore() = user
        override suspend fun login(email: EmailAddress, password: Password) = user
        override suspend fun register(name: FullName, email: EmailAddress, password: Password, invitation: InvitationProof) = user
        override suspend fun profile() = user
        override suspend fun updateProfile(name: FullName, phone: PhoneNumber?) = user
        override suspend fun logout() { }
    }
}
