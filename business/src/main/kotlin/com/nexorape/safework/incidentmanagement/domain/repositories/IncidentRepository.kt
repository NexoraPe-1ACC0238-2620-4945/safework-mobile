package com.nexorape.safework.incidentmanagement.domain.repositories

import com.nexorape.safework.incidentmanagement.domain.model.*

interface IncidentRepository {
    suspend fun list(): List<Incident>
    suspend fun find(id: IncidentId): Incident
    suspend fun report(draft: IncidentDraft): Incident
}

interface LocationProvider { suspend fun capture(): IncidentLocation? }

enum class IncidentFailure {
    INVALID_INPUT, SESSION_INVALID, FORBIDDEN, NOT_FOUND, STATE_CONFLICT, NETWORK,
    SERVER, INVALID_RESPONSE, NOT_CONFIGURED, STORAGE,
}
class IncidentException(val reason: IncidentFailure) : RuntimeException(reason.name)
