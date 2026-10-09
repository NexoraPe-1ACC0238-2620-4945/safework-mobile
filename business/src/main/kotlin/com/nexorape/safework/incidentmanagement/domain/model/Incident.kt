package com.nexorape.safework.incidentmanagement.domain.model

import com.nexorape.safework.iam.domain.model.CompanyId
import com.nexorape.safework.iam.domain.model.UserId

@JvmInline value class IncidentId(val value: Long) { init { require(value > 0) } }
@JvmInline value class IncidentTitle private constructor(val value: String) {
    companion object { fun of(raw: String) = IncidentTitle(text(raw, 120)) }
}
@JvmInline value class IncidentDescription private constructor(val value: String) {
    companion object { fun of(raw: String) = IncidentDescription(text(raw, 4000)) }
}
@JvmInline value class IncidentLocation private constructor(val value: String) {
    companion object { fun of(raw: String) = IncidentLocation(text(raw, 500)) }
}

enum class IncidentStatus {
    OPEN, ASSIGNED, IN_PROGRESS, CLOSED;
    fun permits(next: IncidentStatus) = when (this) {
        OPEN -> next == ASSIGNED
        ASSIGNED -> next == IN_PROGRESS
        IN_PROGRESS -> next == CLOSED
        CLOSED -> false
    }
}

data class Incident(
    val id: IncidentId, val reporterId: UserId, val companyId: CompanyId,
    val title: IncidentTitle, val description: IncidentDescription, val location: IncidentLocation,
    val status: IncidentStatus, val documentUrl: String?, val reporterName: String, val assigneeName: String?,
)

data class IncidentDraft(val title: IncidentTitle, val description: IncidentDescription, val location: IncidentLocation)

private fun text(raw: String, maximum: Int): String {
    val value = raw.trim { Character.isWhitespace(it) }
    require(value.isNotBlank() && value.codePointCount(0, value.length) <= maximum)
    var index = 0
    while (index < value.length) {
        val unit = value[index++]
        if (Character.isHighSurrogate(unit)) require(index < value.length && Character.isLowSurrogate(value[index++]))
        else require(!Character.isLowSurrogate(unit))
    }
    return value
}
