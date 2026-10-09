package com.nexorape.safework.incidentmanagement

import com.nexorape.safework.iam.domain.model.*
import com.nexorape.safework.incidentmanagement.application.*
import com.nexorape.safework.incidentmanagement.domain.model.*
import com.nexorape.safework.incidentmanagement.domain.repositories.*
import com.nexorape.safework.incidentmanagement.presentation.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IncidentViewModelTest {
    @Test fun invalidationDiscardsLateCompanyData() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val actor = UserProfile(UserId(1), CompanyId(2), FullName.of("Synthetic Worker"), EmailAddress.of("synthetic@example.test"), null, setOf(Role.WORKER))
            val response = CompletableDeferred<List<Incident>>()
            val repo = object : IncidentRepository {
                override suspend fun list() = response.await()
                override suspend fun find(id: IncidentId): Incident = error("Not invoked")
                override suspend fun report(draft: IncidentDraft): Incident = error("Not invoked")
            }
            val events = MutableStateFlow(0)
            val location = CaptureIncidentLocation(object : LocationProvider { override suspend fun capture(): IncidentLocation? = null })
            val vm = IncidentViewModel(IncidentUseCases(repo), actor, location, events)
            runCurrent(); events.value++; runCurrent()
            response.complete(listOf(Incident(IncidentId(5), actor.id, actor.companyId, IncidentTitle.of("Synthetic"),
                IncidentDescription.of("Synthetic"), IncidentLocation.of("Manual"), IncidentStatus.OPEN, null, "Synthetic", null)))
            runCurrent()
            assertTrue(vm.state.value.items.isEmpty()); assertNull(vm.state.value.selected)
            assertEquals(IncidentFailure.SESSION_INVALID, vm.state.value.error)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun coordinatesAppendAndMissingFixPreservesManualText() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val actor = UserProfile(UserId(1), CompanyId(2), FullName.of("Synthetic Worker"), EmailAddress.of("synthetic@example.test"), null, setOf(Role.WORKER))
            val repo = object : IncidentRepository {
                override suspend fun list() = emptyList<Incident>()
                override suspend fun find(id: IncidentId): Incident = error("Not invoked")
                override suspend fun report(draft: IncidentDraft): Incident = error("Not invoked")
            }
            var fixes = 0
            val capture = CaptureIncidentLocation(object : LocationProvider {
                override suspend fun capture() = if (fixes++ == 0) IncidentLocation.of("-12.000000, -77.000000") else null
            })
            val vm = IncidentViewModel(IncidentUseCases(repo), actor, capture, MutableStateFlow(0))
            runCurrent(); vm.openReport(); vm.edit(location = "Synthetic gate"); vm.captureLocation(); runCurrent()
            assertEquals("Synthetic gate; -12.000000, -77.000000", vm.state.value.location)
            vm.captureLocation(); runCurrent()
            assertTrue(vm.state.value.locationUnavailable)
            assertEquals("Synthetic gate; -12.000000, -77.000000", vm.state.value.location)
        } finally { Dispatchers.resetMain() }
    }
}
