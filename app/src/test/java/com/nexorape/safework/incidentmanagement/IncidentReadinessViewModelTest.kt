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
class IncidentReadinessViewModelTest {
    private val actor = UserProfile(UserId(1), CompanyId(2), FullName.of("Synthetic Worker"),
        EmailAddress.of("synthetic@example.test"), null, setOf(Role.WORKER))

    private fun incident(id: Long, reporter: Long, status: IncidentStatus) = Incident(
        IncidentId(id), UserId(reporter), actor.companyId, IncidentTitle.of("Synthetic report $id"),
        IncidentDescription.of("Synthetic description"), IncidentLocation.of("Synthetic gate"),
        status, null, "Synthetic Worker", null)

    private class Repository(var items: List<Incident>) : IncidentRepository {
        var pendingSave: CompletableDeferred<Incident>? = null
        override suspend fun list() = items.toList()
        override suspend fun find(id: IncidentId) = items.first { it.id == id }
        override suspend fun report(draft: IncidentDraft): Incident = error("Not invoked")
        override suspend fun updateDocument(id: IncidentId, url: IncidentDocumentUrl): Incident {
            pendingSave?.let { return it.await() }
            val updated = find(id).copy(documentUrl = url.value)
            items = items.map { if (it.id == id) updated else it }
            return updated
        }
    }

    private fun viewModel(repository: Repository, events: MutableStateFlow<Int> = MutableStateFlow(0)) =
        IncidentViewModel(IncidentUseCases(repository), actor,
            CaptureIncidentLocation(object : LocationProvider {
                override suspend fun capture(): IncidentLocation? = null
            }), events)

    @Test fun filtersSurviveDetailRefreshAndDocumentSave() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = Repository(listOf(incident(5, 1, IncidentStatus.OPEN),
                incident(6, 3, IncidentStatus.OPEN), incident(7, 1, IncidentStatus.CLOSED)))
            val vm = viewModel(repository)
            runCurrent()
            vm.filterReporter(true); vm.filterStatus(IncidentStatus.OPEN)
            assertEquals(listOf(IncidentId(5)), vm.state.value.visibleItems.map { it.id })
            vm.detail(IncidentId(5)); runCurrent()
            vm.openDocument(); vm.editDocument("https://example.test/manual.pdf"); vm.saveDocument(); runCurrent()
            assertEquals(IncidentPage.DETAIL, vm.state.value.page)
            assertTrue(vm.state.value.documentSaved)
            assertEquals("https://example.test/manual.pdf", vm.state.value.items.first().documentUrl)
            vm.back(); vm.refresh(); runCurrent()
            assertEquals(listOf(IncidentId(5)), vm.state.value.visibleItems.map { it.id })
            vm.filterStatus(IncidentStatus.ASSIGNED)
            assertTrue(vm.state.value.visibleItems.isEmpty())
            assertEquals(3, vm.state.value.items.size)
            vm.clearFilters()
            assertEquals(3, vm.state.value.visibleItems.size)
        } finally { Dispatchers.resetMain() }
    }

    @Test fun cancellationDoesNotSaveAndLogoutDiscardsLateDocumentResponse() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val original = incident(5, 1, IncidentStatus.OPEN)
            val repository = Repository(listOf(original))
            val events = MutableStateFlow(0)
            val vm = viewModel(repository, events)
            runCurrent(); vm.detail(original.id); runCurrent()
            vm.openDocument(); vm.editDocument("https://example.test/manual.pdf"); vm.back()
            assertNull(repository.items.single().documentUrl)
            assertEquals(IncidentPage.DETAIL, vm.state.value.page)
            val deferred = CompletableDeferred<Incident>()
            repository.pendingSave = deferred
            vm.openDocument(); vm.editDocument("https://example.test/manual.pdf"); vm.saveDocument(); runCurrent()
            events.value++; runCurrent()
            deferred.complete(original.copy(documentUrl = "https://example.test/manual.pdf")); runCurrent()
            assertNull(vm.state.value.selected)
            assertTrue(vm.state.value.items.isEmpty())
            assertFalse(vm.state.value.documentSaved)
            assertEquals(IncidentFailure.SESSION_INVALID, vm.state.value.error)
        } finally { Dispatchers.resetMain() }
    }
}
