package com.nexorape.safework.iam.infrastructure.http

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.nexorape.safework.core.network.*
import com.nexorape.safework.iam.domain.model.*
import com.nexorape.safework.iam.domain.repositories.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HttpCompanyRepository(private val api: ApiClient) : CompanyRepository {
    override suspend fun search(query: String, page: Int) = guarded {
        api.request("GET", "api/v1/companies", query = mapOf("search" to query, "page" to "$page", "size" to "20"))!!
            .asJsonArray.map(::companyFrom)
    }
    override suspend fun company(id: CompanyId) = guarded {
        companyFrom(api.request("GET", "api/v1/companies/${id.value}")!!)
    }
    override suspend fun create(name: CompanyName) = guarded {
        companyFrom(api.request("POST", "api/v1/companies", Gson().toJson(mapOf("name" to name.value)), expected = 201)!!)
    }
    override suspend fun request(company: CompanyId) = guarded {
        requestFrom(api.request("POST", "api/v1/companies/${company.value}/membership-requests", expected = 201)!!)
    }
    override suspend fun ownRequests(page: Int) = requests("api/v1/users/me/membership-requests", page)
    override suspend fun companyRequests(company: CompanyId, page: Int) = requests("api/v1/companies/${company.value}/membership-requests", page)
    private suspend fun requests(path: String, page: Int) = guarded {
        api.request("GET", path, query = mapOf("page" to "$page", "size" to "20"))!!.asJsonArray.map(::requestFrom)
    }
    override suspend fun decide(company: CompanyId, requestId: Long, accept: Boolean) = guarded {
        val decision = if (accept) "accept" else "reject"
        requestFrom(api.request("POST", "api/v1/companies/${company.value}/membership-requests/$requestId/$decision")!!)
    }
    override suspend fun members(company: CompanyId, page: Int) = guarded {
        api.request("GET", "api/v1/companies/${company.value}/members", query = mapOf("page" to "$page", "size" to "20"))!!
            .asJsonArray.map { UserDto.parse(it).toDomain() }
    }
    override suspend fun revoke(company: CompanyId, user: UserId) = guarded {
        UserDto.parse(api.request("DELETE", "api/v1/companies/${company.value}/members/${user.value}")!!).toDomain()
    }

    private fun companyFrom(value: JsonElement): CompanySummary {
        val json = value.asJsonObject
        require(json["name"].asJsonPrimitive.isString)
        return CompanySummary(CompanyId(json["id"].asBigDecimal.longValueExact()), CompanyName.of(json["name"].asString))
    }
    private fun requestFrom(value: JsonElement): MembershipRequest {
        val json = value.asJsonObject
        fun id(key: String) = json[key].asBigDecimal.longValueExact()
        fun text(key: String): String {
            require(json[key].asJsonPrimitive.isString)
            return json[key].asString
        }
        return MembershipRequest(id("id"), CompanyId(id("companyId")), UserId(id("userId")),
            FullName.of(text("fullName")), EmailAddress.of(text("email")), RequestStatus.valueOf(text("status")))
    }
    private suspend fun <T> guarded(block: suspend () -> T): T = withContext(Dispatchers.IO) {
        try { block() }
        catch (e: CancellationException) { throw e }
        catch (e: IdentityException) { throw e }
        catch (_: SessionStorageException) { throw IdentityException(IdentityFailure.STORAGE) }
        catch (e: ApiException) { throw IdentityException(IdentityFailure.entries.find { it.name == e.code } ?: IdentityFailure.SERVER) }
        catch (_: IllegalArgumentException) { throw IdentityException(IdentityFailure.INVALID_RESPONSE) }
        catch (_: IllegalStateException) { throw IdentityException(IdentityFailure.INVALID_RESPONSE) }
        catch (_: NullPointerException) { throw IdentityException(IdentityFailure.INVALID_RESPONSE) }
    }
}
