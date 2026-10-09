package com.nexorape.safework.iam.infrastructure.http

import com.google.gson.JsonElement
import com.nexorape.safework.iam.domain.model.*

internal data class UserDto(val id: Long, val companyId: Long, val fullName: String, val email: String,
                            val phoneNumber: String?, val roles: List<String>) {
    fun toDomain() = UserProfile(UserId(id), CompanyId(companyId), FullName.of(fullName), EmailAddress.of(email),
        phoneNumber?.let(PhoneNumber::of), roles.map(Role::valueOf).toSet().also { require(it.isNotEmpty()) })

    companion object {
        fun parse(value: JsonElement): UserDto {
            val json = value.asJsonObject
            fun string(key: String): String = json[key].also { require(it.isJsonPrimitive && it.asJsonPrimitive.isString) }.asString
            fun id(key: String): Long = json[key].asBigDecimal.longValueExact().also { require(it > 0) }
            return UserDto(id("id"), id("companyId"), string("fullName"), string("email"),
                json["phoneNumber"]?.takeUnless { it.isJsonNull }?.asString,
                json["roles"].asJsonArray.map { it.asString })
        }
    }
}
