package com.nexorape.safework.incidentmanagement.application

import com.nexorape.safework.iam.domain.model.*
import com.nexorape.safework.incidentmanagement.domain.model.*
import com.nexorape.safework.incidentmanagement.domain.repositories.*

class IncidentHandlingUseCases(private val repository: IncidentHandlingRepository) {
    suspend fun ownAssignments(actor: UserProfile): List<Assignment> {
        if (Role.WORKER !in actor.roles && Role.EMPLOYER !in actor.roles) failure(IncidentFailure.FORBIDDEN)
        return repository.ownAssignments().also { list ->
            if (list.any { it.responsibleId != actor.id }) failure(IncidentFailure.INVALID_RESPONSE)
        }
    }
    suspend fun assignment(id: AssignmentId, actor: UserProfile): Assignment {
        employer(actor)
        return repository.assignment(id).also {
            if (it.id != id || it.responsibleId != actor.id) failure(IncidentFailure.INVALID_RESPONSE)
        }
    }
    suspend fun take(incident: Incident, actor: UserProfile): Assignment {
        employer(actor); sameCompany(incident, actor)
        if (!incident.status.permits(IncidentStatus.ASSIGNED)) failure(IncidentFailure.STATE_CONFLICT)
        return repository.take(incident.id).also {
            if (it.incidentId != incident.id || it.responsibleId != actor.id || it.status != IncidentStatus.ASSIGNED)
                failure(IncidentFailure.INVALID_RESPONSE)
        }
    }
    suspend fun start(incident: Incident, assignment: Assignment, actor: UserProfile) = transition(incident, assignment, actor, IncidentStatus.IN_PROGRESS)
    suspend fun close(incident: Incident, assignment: Assignment, actor: UserProfile) = transition(incident, assignment, actor, IncidentStatus.CLOSED)
    private suspend fun transition(incident: Incident, assignment: Assignment, actor: UserProfile, next: IncidentStatus): Incident {
        employer(actor); sameCompany(incident, actor)
        if (assignment.responsibleId != actor.id || assignment.incidentId != incident.id) failure(IncidentFailure.FORBIDDEN)
        if (!incident.status.permits(next) || assignment.status != incident.status) failure(IncidentFailure.STATE_CONFLICT)
        val response = if (next == IncidentStatus.IN_PROGRESS) repository.start(incident.id) else repository.close(incident.id)
        if (response.id != incident.id || response.companyId != actor.companyId || response.status != next || response.reporterId != incident.reporterId)
            failure(IncidentFailure.INVALID_RESPONSE)
        return response
    }
    private fun employer(actor: UserProfile) { if (Role.EMPLOYER !in actor.roles) failure(IncidentFailure.FORBIDDEN) }
    private fun sameCompany(incident: Incident, actor: UserProfile) { if (incident.companyId != actor.companyId) failure(IncidentFailure.NOT_FOUND) }
    private fun failure(reason: IncidentFailure): Nothing = throw IncidentException(reason)
}
