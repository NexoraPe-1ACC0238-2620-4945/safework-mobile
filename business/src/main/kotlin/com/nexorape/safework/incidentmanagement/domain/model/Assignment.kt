package com.nexorape.safework.incidentmanagement.domain.model

import com.nexorape.safework.iam.domain.model.UserId
import java.time.Instant

@JvmInline value class AssignmentId(val value: Long) { init { require(value > 0) } }
enum class AssignmentPriority { LOW, MEDIUM, HIGH }
data class Assignment(
    val id: AssignmentId, val incidentId: IncidentId, val responsibleId: UserId, val incidentTitle: String,
    val status: IncidentStatus, val assignedAt: Instant, val priority: AssignmentPriority, val completionDate: Instant?,
) {
    init {
        require(incidentTitle.isNotBlank())
        require(status != IncidentStatus.OPEN)
        require((completionDate != null) == (status == IncidentStatus.CLOSED))
    }
}
