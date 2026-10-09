package com.nexorape.safework.iam.domain.repositories

import com.nexorape.safework.iam.domain.model.*

interface IdentityRepository {
    suspend fun restore(): UserProfile?
    suspend fun login(email: EmailAddress, password: Password): UserProfile
    suspend fun register(name: FullName, email: EmailAddress, password: Password, invitation: InvitationProof): UserProfile
    suspend fun profile(): UserProfile
    suspend fun updateProfile(name: FullName, phone: PhoneNumber?): UserProfile
    suspend fun logout(): LogoutOutcome
}

enum class LogoutOutcome { SERVER_CONFIRMED, LOCAL_ONLY }

enum class IdentityFailure {
    INVALID_INPUT, INVALID_CREDENTIALS, SESSION_INVALID, FORBIDDEN, EMAIL_UNAVAILABLE,
    INVITATION_INVALID, RATE_LIMITED, NETWORK, SERVER, INVALID_RESPONSE, STORAGE, NOT_CONFIGURED,
}

class IdentityException(val reason: IdentityFailure) : RuntimeException(reason.name)
