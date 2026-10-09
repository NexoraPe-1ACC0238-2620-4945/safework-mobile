package com.nexorape.safework.incidentmanagement.application

import com.nexorape.safework.iam.domain.model.*
import com.nexorape.safework.incidentmanagement.domain.model.*
import com.nexorape.safework.incidentmanagement.domain.repositories.*

class CaptureIncidentLocation(private val provider: LocationProvider) {
    suspend fun capture() = provider.capture()
}

class IncidentUseCases(private val repository: IncidentRepository) {
    suspend fun list(actor: UserProfile): List<Incident> {
        requireReader(actor)
        return repository.list().also { list -> list.forEach { validateCompany(it, actor) } }.toList()
    }
    suspend fun detail(id: IncidentId, actor: UserProfile): Incident {
        requireReader(actor)
        return repository.find(id).also { validateCompany(it, actor); if (it.id != id) invalidResponse() }
    }
    suspend fun report(title: String, description: String, location: String, actor: UserProfile): Incident {
        requireReader(actor)
        val draft = try { IncidentDraft(IncidentTitle.of(title), IncidentDescription.of(description), IncidentLocation.of(location)) }
        catch (_: IllegalArgumentException) { throw IncidentException(IncidentFailure.INVALID_INPUT) }
        return repository.report(draft).also {
            validateCompany(it, actor)
            if (it.reporterId != actor.id || it.status != IncidentStatus.OPEN) invalidResponse()
        }
    }
    private fun requireReader(actor: UserProfile) {
        if (Role.WORKER !in actor.roles && Role.EMPLOYER !in actor.roles) throw IncidentException(IncidentFailure.FORBIDDEN)
    }
    private fun validateCompany(incident: Incident, actor: UserProfile) {
        if (incident.companyId != actor.companyId) invalidResponse()
    }
    private fun invalidResponse(): Nothing = throw IncidentException(IncidentFailure.INVALID_RESPONSE)
}
