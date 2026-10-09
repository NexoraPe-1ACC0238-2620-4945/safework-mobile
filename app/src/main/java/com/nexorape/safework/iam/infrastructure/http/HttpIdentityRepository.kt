package com.nexorape.safework.iam.infrastructure.http

import com.google.gson.Gson
import com.nexorape.safework.core.network.*
import com.nexorape.safework.iam.domain.model.*
import com.nexorape.safework.iam.domain.repositories.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext

class HttpIdentityRepository(private val api: ApiClient, private val sessions: SessionStore) : IdentityRepository {
    private val gson = Gson()

    override suspend fun restore(): UserProfile? = guarded {
        if (sessions.read() == null) null else profile()
    }

    override suspend fun login(email: EmailAddress, password: Password): UserProfile = guarded {
        val response = api.request("POST", "api/v1/authentication/sign-in",
            gson.toJson(mapOf("email" to email.value, "password" to password.value)), authenticated = false)!!.asJsonObject
        val token = response["token"].asString.also { require(it.isNotBlank() && it.length <= 16384) }
        // Fetch authoritative profile before persisting credentials or exposing signed-in UI.
        try {
            val user = UserDto.parse(api.request("GET", "api/v1/users/me", tokenOverride = token)!!).toDomain()
            require(response["id"].asBigDecimal.longValueExact() == user.id.value)
            try { sessions.write(SessionCredential(token, api.baseUrl)) }
            catch (_: Exception) { throw IdentityException(IdentityFailure.STORAGE) }
            user
        } catch (failure: Exception) {
            // A new session that cannot be validated/saved is rolled back best effort.
            withContext(NonCancellable) {
                try { api.request("POST", "api/v1/authentication/sign-out", tokenOverride = token, expected = 204) }
                catch (_: Exception) { /* Expiry still bounds a session when the server is unreachable. */ }
            }
            throw failure
        }
    }

    override suspend fun register(name: FullName, email: EmailAddress, password: Password,
                                  invitation: InvitationProof): UserProfile = guarded {
        val body = gson.toJson(mapOf("fullName" to name.value, "emailAddress" to email.value,
            "password" to password.value, "invitationToken" to invitation.value))
        UserDto.parse(api.request("POST", "api/v1/authentication/sign-up", body, authenticated = false, expected = 201)!!).toDomain()
            .also { require(it.roles == setOf(Role.WORKER)) }
    }

    override suspend fun profile(): UserProfile = guarded {
        UserDto.parse(api.request("GET", "api/v1/users/me")!!).toDomain()
    }

    override suspend fun updateProfile(name: FullName, phone: PhoneNumber?): UserProfile = guarded {
        val fields = mutableMapOf("fullName" to name.value)
        phone?.let { fields["phoneNumber"] = it.value }
        UserDto.parse(api.request("PATCH", "api/v1/users/me", gson.toJson(fields))!!).toDomain()
    }

    override suspend fun logout(): Unit = guarded {
        val credential = sessions.read() ?: return@guarded
        try { api.request("POST", "api/v1/authentication/sign-out", expected = 204) }
        catch (e: ApiException) { if (e.status != 401) throw e }
        sessions.clear(credential.token)
    }

    private suspend fun <T> guarded(block: suspend () -> T): T = withContext(Dispatchers.IO) {
        try { block() }
        catch (e: IdentityException) { throw e }
        catch (e: CancellationException) { throw e }
        catch (_: SessionStorageException) { throw IdentityException(IdentityFailure.STORAGE) }
        catch (e: ApiException) { throw IdentityException(IdentityFailure.entries.find { it.name == e.code } ?: IdentityFailure.SERVER) }
        catch (_: IllegalArgumentException) { throw IdentityException(IdentityFailure.INVALID_RESPONSE) }
        catch (_: IllegalStateException) { throw IdentityException(IdentityFailure.INVALID_RESPONSE) }
        catch (_: NullPointerException) { throw IdentityException(IdentityFailure.INVALID_RESPONSE) }
    }
}
