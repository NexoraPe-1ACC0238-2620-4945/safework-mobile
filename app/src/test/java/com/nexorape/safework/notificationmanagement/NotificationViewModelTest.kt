package com.nexorape.safework.notificationmanagement

import com.nexorape.safework.iam.domain.model.*
import com.nexorape.safework.notificationmanagement.application.GetUserNotifications
import com.nexorape.safework.notificationmanagement.domain.model.UserNotification
import com.nexorape.safework.notificationmanagement.domain.repositories.*
import com.nexorape.safework.notificationmanagement.presentation.NotificationViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationViewModelTest {
    @Test fun logoutClearsRecipientDataAndDiscardsLateResponse() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val response = CompletableDeferred<List<UserNotification>>()
            val repository = object : NotificationRepository { override suspend fun ownNotifications() = response.await() }
            val actor = UserProfile(UserId(1), CompanyId(2), FullName.of("Synthetic"), EmailAddress.of("synthetic@example.test"), null, setOf(Role.WORKER))
            val events = MutableStateFlow(0)
            val vm = NotificationViewModel(GetUserNotifications(repository), actor, events)
            runCurrent(); events.value++; runCurrent()
            response.complete(listOf(UserNotification(UUID.randomUUID(), "Synthetic", "Synthetic", Instant.EPOCH, false))); runCurrent()
            assertTrue(vm.state.value.items.isEmpty()); assertEquals(NotificationFailure.SESSION_INVALID, vm.state.value.error)
        } finally { Dispatchers.resetMain() }
    }
}
