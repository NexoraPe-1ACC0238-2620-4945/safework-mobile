package com.nexorape.safework.notificationmanagement.domain.repositories

import com.nexorape.safework.notificationmanagement.domain.model.UserNotification

interface NotificationRepository { suspend fun ownNotifications(): List<UserNotification> }
enum class NotificationFailure { SESSION_INVALID, FORBIDDEN, NETWORK, SERVER, INVALID_RESPONSE, NOT_CONFIGURED, STORAGE }
class NotificationException(val reason: NotificationFailure) : RuntimeException(reason.name)
