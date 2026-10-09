package com.nexorape.safework.incidentmanagement.domain.repositories

import com.nexorape.safework.incidentmanagement.domain.model.*

/** Same Incident model/context as query/reporting; no selected-responsible parameter. */
interface IncidentHandlingRepository {
    suspend fun ownAssignments(): List<Assignment>
    suspend fun assignment(id: AssignmentId): Assignment
    suspend fun take(id: IncidentId): Assignment
    suspend fun start(id: IncidentId): Incident
    suspend fun close(id: IncidentId): Incident
}
