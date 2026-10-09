package com.nexorape.safework.incidentmanagement.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexorape.safework.iam.domain.model.UserProfile
import com.nexorape.safework.incidentmanagement.application.IncidentHandlingUseCases
import com.nexorape.safework.incidentmanagement.application.IncidentUseCases
import com.nexorape.safework.incidentmanagement.domain.model.*
import com.nexorape.safework.incidentmanagement.domain.repositories.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class IncidentHandlingState(val incident: Incident? = null, val assignment: Assignment? = null,
                                val busy: Boolean = false, val error: IncidentFailure? = null)

class IncidentHandlingViewModel(private val cases: IncidentHandlingUseCases, private val queries: IncidentUseCases,
                                actor: UserProfile, invalidations: StateFlow<Int>) : ViewModel() {
    private val mutable = MutableStateFlow(IncidentHandlingState())
    val state = mutable.asStateFlow()
    private var identity: UserProfile? = actor
    private var generation = 0
    private var operation: Job? = null
    private var pendingIncident: Incident? = null
    init {
        viewModelScope.launch { invalidations.drop(1).collect { identity = null; hide() } }
    }
    fun observe(incident: Incident) {
        if (state.value.busy) { if (state.value.incident != incident) pendingIncident = incident; return }
        val actor = identity ?: return
        generation++
        val revision = generation
        mutable.value = IncidentHandlingState(incident = incident, busy = true)
        operation = viewModelScope.launch {
            try {
                val owned = cases.ownAssignments(actor).firstOrNull { it.incidentId == incident.id }
                if (generation == revision) mutable.value = state.value.copy(assignment = owned)
            } catch (e: CancellationException) { throw e }
            catch (e: IncidentException) { if (generation == revision) mutable.value = state.value.copy(error = e.reason) }
            finally { finish(revision) }
        }
    }
    fun take(changed: () -> Unit) = mutate(changed) { incident, _, actor -> cases.take(incident, actor) }
    fun start(changed: () -> Unit) = mutate(changed) { incident, assignment, actor ->
        cases.start(incident, assignment ?: throw IncidentException(IncidentFailure.FORBIDDEN), actor)
    }
    fun close(changed: () -> Unit) = mutate(changed) { incident, assignment, actor ->
        cases.close(incident, assignment ?: throw IncidentException(IncidentFailure.FORBIDDEN), actor)
    }
    fun hide() { generation++; pendingIncident = null; operation?.cancel(); mutable.value = IncidentHandlingState() }
    private fun mutate(changed: () -> Unit, action: suspend (Incident, Assignment?, UserProfile) -> Any) {
        if (state.value.busy) return
        val incident = state.value.incident ?: return
        val actor = identity ?: return
        val revision = generation
        mutable.value = state.value.copy(busy = true, error = null)
        operation = viewModelScope.launch {
            try {
                action(incident, state.value.assignment, actor)
                val current = queries.detail(incident.id, actor)
                val assignment = cases.ownAssignments(actor).firstOrNull { it.incidentId == incident.id }
                if (generation == revision) {
                    mutable.value = IncidentHandlingState(current, assignment)
                    changed()
                }
            } catch (e: CancellationException) { throw e }
            catch (e: IncidentException) { if (generation == revision) mutable.value = state.value.copy(error = e.reason) }
            finally { finish(revision) }
        }
    }
    private fun finish(revision: Int) {
        if (generation != revision) return
        mutable.value = state.value.copy(busy = false)
        val queued = pendingIncident
        pendingIncident = null
        if (queued != null) observe(queued)
    }
}
