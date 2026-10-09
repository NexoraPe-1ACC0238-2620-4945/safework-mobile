package com.nexorape.safework.incidentmanagement.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexorape.safework.iam.domain.model.UserProfile
import com.nexorape.safework.incidentmanagement.application.IncidentUseCases
import com.nexorape.safework.incidentmanagement.application.CaptureIncidentLocation
import com.nexorape.safework.incidentmanagement.domain.model.*
import com.nexorape.safework.incidentmanagement.domain.repositories.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class IncidentPage { LIST, DETAIL, REPORT }
data class IncidentUiState(
    val page: IncidentPage = IncidentPage.LIST, val items: List<Incident> = emptyList(), val selected: Incident? = null,
    val busy: Boolean = false, val error: IncidentFailure? = null, val reported: Boolean = false,
    val title: String = "", val description: String = "", val location: String = "",
    val locating: Boolean = false, val locationUnavailable: Boolean = false,
)

class IncidentViewModel(private val cases: IncidentUseCases, actor: UserProfile,
                        private val locationCapture: CaptureIncidentLocation, invalidations: StateFlow<Int>) : ViewModel() {
    private val mutable = MutableStateFlow(IncidentUiState())
    val state = mutable.asStateFlow()
    private var locationJob: Job? = null
    private var identity: UserProfile? = actor
    private var generation = 0
    init {
        viewModelScope.launch {
            invalidations.drop(1).collect {
                generation++; identity = null; cancelLocation()
                mutable.value = IncidentUiState(error = IncidentFailure.SESSION_INVALID)
            }
        }
        refresh()
    }
    fun refresh() = runAction {
        val items = cases.list(it)
        state.value.copy(items = items, page = IncidentPage.LIST, selected = null, error = null)
    }
    fun detail(id: IncidentId) = runAction {
        state.value.copy(selected = cases.detail(id, it), page = IncidentPage.DETAIL)
    }
    fun openReport() {
        if (state.value.busy) return
        mutable.value = state.value.copy(page = IncidentPage.REPORT, error = null, reported = false)
    }
    fun back() {
        if (state.value.busy) return
        cancelLocation()
        mutable.value = state.value.copy(page = IncidentPage.LIST, selected = null, error = null)
    }
    fun edit(title: String = state.value.title, description: String = state.value.description,
             location: String = state.value.location) {
        if (location != state.value.location && state.value.locating) cancelLocation()
        if (!state.value.busy) mutable.value = state.value.copy(title = title, description = description, location = location)
    }
    fun report() = runAction {
        cancelLocation()
        val incident = cases.report(state.value.title, state.value.description, state.value.location, it)
        state.value.copy(page = IncidentPage.DETAIL, selected = incident,
            items = listOf(incident) + state.value.items, title = "", description = "", location = "", reported = true)
    }
    fun captureLocation() {
        if (state.value.locating || state.value.busy || identity == null) return
        mutable.value = state.value.copy(locating = true, locationUnavailable = false)
        locationJob = viewModelScope.launch {
            try {
                val coordinates = locationCapture.capture()
                val combined = if (coordinates == null) state.value.location else
                    listOf(state.value.location.trim(), coordinates.value).filter { it.isNotEmpty() }.joinToString("; ")
                mutable.value = state.value.copy(location = combined, locationUnavailable = coordinates == null)
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutable.value = state.value.copy(locationUnavailable = true) }
            finally { mutable.value = state.value.copy(locating = false) }
        }
    }
    fun denyLocation() { mutable.value = state.value.copy(locationUnavailable = true) }
    fun stopCapture() = cancelLocation()
    private fun cancelLocation() { locationJob?.cancel(); mutable.value = state.value.copy(locating = false) }
    private fun runAction(action: suspend (UserProfile) -> IncidentUiState) {
        if (state.value.busy) return
        val actor = identity ?: return
        val startedGeneration = generation
        mutable.value = state.value.copy(busy = true, error = null)
        viewModelScope.launch {
            try { val result = action(actor); if (generation == startedGeneration) mutable.value = result }
            catch (e: CancellationException) { throw e }
            catch (e: IncidentException) { if (generation == startedGeneration) mutable.value = state.value.copy(error = e.reason) }
            finally { mutable.value = state.value.copy(busy = false) }
        }
    }
}
