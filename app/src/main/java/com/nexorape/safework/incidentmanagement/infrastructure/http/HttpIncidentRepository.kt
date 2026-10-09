package com.nexorape.safework.incidentmanagement.infrastructure.http

import com.google.gson.Gson
import com.nexorape.safework.core.network.*
import com.nexorape.safework.incidentmanagement.domain.model.*
import com.nexorape.safework.incidentmanagement.domain.repositories.*
import kotlinx.coroutines.*

class HttpIncidentRepository(private val api: ApiClient) : IncidentRepository {
    override suspend fun list() = incidentCall {
        api.request("GET", "api/v1/incidents")!!.asJsonArray.map(IncidentDto::toDomain)
    }
    override suspend fun find(id: IncidentId) = incidentCall {
        IncidentDto.toDomain(api.request("GET", "api/v1/incidents/${id.value}")!!)
    }
    override suspend fun report(draft: IncidentDraft) = incidentCall {
        val json = Gson().toJson(mapOf("title" to draft.title.value, "description" to draft.description.value, "location" to draft.location.value))
        IncidentDto.toDomain(api.request("POST", "api/v1/incidents", json, expected = 201)!!)
    }
}

internal suspend fun <T> incidentCall(action: suspend () -> T): T = withContext(Dispatchers.IO) {
    try { action() }
    catch (e: CancellationException) { throw e }
    catch (e: IncidentException) { throw e }
    catch (_: SessionStorageException) { throw IncidentException(IncidentFailure.STORAGE) }
    catch (e: ApiException) {
        val reason = when (e.status) {
            400 -> IncidentFailure.INVALID_INPUT; 401 -> IncidentFailure.SESSION_INVALID
            403 -> IncidentFailure.FORBIDDEN; 404 -> IncidentFailure.NOT_FOUND; 409 -> IncidentFailure.STATE_CONFLICT
            else -> IncidentFailure.entries.find { it.name == e.code } ?: IncidentFailure.SERVER
        }
        throw IncidentException(reason)
    }
    catch (_: RuntimeException) { throw IncidentException(IncidentFailure.INVALID_RESPONSE) }
}
