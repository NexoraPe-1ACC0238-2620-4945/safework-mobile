package com.nexorape.safework.incidentmanagement

import com.nexorape.safework.iam.domain.model.*
import com.nexorape.safework.incidentmanagement.application.*
import com.nexorape.safework.incidentmanagement.domain.model.*
import com.nexorape.safework.incidentmanagement.domain.repositories.*
import com.nexorape.safework.incidentmanagement.presentation.IncidentHandlingViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HandlingViewModelTest {
    @Test fun logoutClearsResponsibilityAndCancelsPendingLoad() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val delayed = CompletableDeferred<List<Assignment>>()
            val repository = object : IncidentHandlingRepository {
                override suspend fun ownAssignments() = delayed.await()
                override suspend fun assignment(id: AssignmentId): Assignment = error("Not invoked")
                override suspend fun take(id: IncidentId): Assignment = error("Not invoked")
                override suspend fun start(id: IncidentId): Incident = error("Not invoked")
                override suspend fun close(id: IncidentId): Incident = error("Not invoked")
            }
            val queries = object : IncidentRepository {
                override suspend fun list() = emptyList<Incident>()
                override suspend fun find(id: IncidentId): Incident = error("Not invoked")
                override suspend fun report(draft: IncidentDraft): Incident = error("Not invoked")
            }
            val actor = UserProfile(UserId(3), CompanyId(2), FullName.of("Synthetic"), EmailAddress.of("synthetic@example.test"), null, setOf(Role.EMPLOYER))
            val events = MutableStateFlow(0)
            val vm = IncidentHandlingViewModel(IncidentHandlingUseCases(repository), IncidentUseCases(queries), actor, events)
            runCurrent()
            vm.observe(Incident(IncidentId(5), UserId(1), actor.companyId, IncidentTitle.of("Synthetic"), IncidentDescription.of("Synthetic"),
                IncidentLocation.of("Manual"), IncidentStatus.ASSIGNED, null, "Synthetic", "Synthetic")); runCurrent()
            events.value++; runCurrent(); delayed.complete(emptyList()); runCurrent()
            assertNull(vm.state.value.incident); assertNull(vm.state.value.assignment); assertFalse(vm.state.value.busy)
        } finally { Dispatchers.resetMain() }
    }
}
