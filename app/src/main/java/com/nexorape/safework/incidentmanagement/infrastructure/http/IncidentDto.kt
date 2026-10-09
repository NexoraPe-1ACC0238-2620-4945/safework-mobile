package com.nexorape.safework.incidentmanagement.infrastructure.http

import com.google.gson.JsonElement
import com.nexorape.safework.iam.domain.model.*
import com.nexorape.safework.incidentmanagement.domain.model.*

internal object IncidentDto {
    fun toDomain(value: JsonElement): Incident {
        val json = value.asJsonObject
        fun id(key: String) = json[key].asBigDecimal.longValueExact().also { require(it > 0) }
        fun string(key: String): String = json[key].also { require(it.isJsonPrimitive && it.asJsonPrimitive.isString) }.asString
        fun optional(key: String) = json[key]?.takeUnless { it.isJsonNull }?.asString
        return Incident(IncidentId(id("id")), UserId(id("userId")), CompanyId(id("companyId")),
            IncidentTitle.of(string("title")), IncidentDescription.of(string("description")), IncidentLocation.of(string("location")),
            IncidentStatus.valueOf(string("status")), optional("documentUrl"), string("reporterName"), optional("assigneeName"))
    }
}
