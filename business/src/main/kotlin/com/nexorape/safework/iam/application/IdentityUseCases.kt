package com.nexorape.safework.iam.application

import com.nexorape.safework.iam.domain.model.*
import com.nexorape.safework.iam.domain.repositories.*

class IdentityUseCases(private val repository: IdentityRepository) {
    suspend fun restore() = repository.restore()
    suspend fun login(email: String, password: String) = validated {
        repository.login(EmailAddress.of(email), Password.forLogin(password))
    }
    suspend fun register(name: String, email: String, password: String, invitation: String) = validated {
        repository.register(FullName.of(name), EmailAddress.of(email), Password.forRegistration(password), InvitationProof.of(invitation))
    }
    suspend fun profile() = repository.profile()
    suspend fun updateProfile(name: String, phone: String) = validated {
        repository.updateProfile(FullName.of(name), phone.takeIf { it.isNotBlank() }?.let(PhoneNumber::of))
    }
    suspend fun logout() = repository.logout()

    private suspend fun <T> validated(action: suspend () -> T): T = try {
        action()
    } catch (_: IllegalArgumentException) {
        throw IdentityException(IdentityFailure.INVALID_INPUT)
    }
}
