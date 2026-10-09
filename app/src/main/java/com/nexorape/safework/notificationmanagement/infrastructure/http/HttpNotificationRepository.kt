package com.nexorape.safework.notificationmanagement.infrastructure.http

import com.nexorape.safework.core.network.*
import com.nexorape.safework.notificationmanagement.domain.model.UserNotification
import com.nexorape.safework.notificationmanagement.domain.repositories.*
import kotlinx.coroutines.*
import java.time.OffsetDateTime
import java.util.UUID

class HttpNotificationRepository(private val api: ApiClient) : NotificationRepository {
    override suspend fun ownNotifications(): List<UserNotification> = withContext(Dispatchers.IO) {
        try {
            api.request("GET", "api/v1/notifications/my-notifications")!!.asJsonArray.map { value ->
                val json = value.asJsonObject
                fun string(key: String) = json[key].also { require(it.isJsonPrimitive && it.asJsonPrimitive.isString) }.asString
                val read = json["isRead"].also { require(it.isJsonPrimitive && it.asJsonPrimitive.isBoolean) }.asBoolean
                UserNotification(UUID.fromString(string("id")), string("subject"), string("body"), OffsetDateTime.parse(string("createdAt")).toInstant(), read)
            }
        } catch (e: CancellationException) { throw e }
        catch (_: SessionStorageException) { throw NotificationException(NotificationFailure.STORAGE) }
        catch (e: ApiException) { throw NotificationException(NotificationFailure.entries.find { it.name == e.code } ?: NotificationFailure.SERVER) }
        catch (_: RuntimeException) { throw NotificationException(NotificationFailure.INVALID_RESPONSE) }
    }
}
