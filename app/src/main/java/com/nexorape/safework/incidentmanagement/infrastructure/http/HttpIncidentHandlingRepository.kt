package com.nexorape.safework.incidentmanagement.infrastructure.http

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.nexorape.safework.core.network.ApiClient
import com.nexorape.safework.iam.domain.model.UserId
import com.nexorape.safework.incidentmanagement.domain.model.*
import com.nexorape.safework.incidentmanagement.domain.repositories.IncidentHandlingRepository
import java.time.OffsetDateTime

class HttpIncidentHandlingRepository(private val api: ApiClient) : IncidentHandlingRepository {
    override suspend fun ownAssignments() = incidentCall {
        api.request("GET", "api/v1/assignments")!!.asJsonArray.map(AssignmentDto::toDomain)
    }
    override suspend fun assignment(id: AssignmentId) = incidentCall {
        AssignmentDto.toDomain(api.request("GET", "api/v1/assignments/${id.value}")!!)
    }
    override suspend fun take(id: IncidentId) = incidentCall {
        AssignmentDto.toDomain(api.request("POST", "api/v1/assignments", Gson().toJson(mapOf("incidentId" to id.value)), expected = 201)!!)
    }
    override suspend fun start(id: IncidentId) = incidentCall {
        IncidentDto.toDomain(api.request("POST", "api/v1/incidents/${id.value}/start")!!)
    }
    override suspend fun close(id: IncidentId) = incidentCall {
        IncidentDto.toDomain(api.request("POST", "api/v1/incidents/${id.value}/close")!!)
    }
}

internal object AssignmentDto {
    fun toDomain(value: JsonElement): Assignment {
        val json = value.asJsonObject
        fun id(key: String) = json[key].asBigDecimal.longValueExact().also { require(it > 0) }
        fun string(key: String) = json[key].also { require(it.isJsonPrimitive && it.asJsonPrimitive.isString) }.asString
        return Assignment(AssignmentId(id("id")), IncidentId(id("incidentId")), UserId(id("userId")), string("incidentTitle"),
            IncidentStatus.valueOf(string("status")), OffsetDateTime.parse(string("assignedAt")).toInstant(),
            AssignmentPriority.valueOf(string("priority")), json["completionDate"]?.takeUnless { it.isJsonNull }?.asString?.let { OffsetDateTime.parse(it).toInstant() })
    }
}
