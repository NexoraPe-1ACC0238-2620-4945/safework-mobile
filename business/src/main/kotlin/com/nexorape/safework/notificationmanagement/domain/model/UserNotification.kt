package com.nexorape.safework.notificationmanagement.domain.model

import java.time.Instant
import java.util.UUID

data class UserNotification(val id: UUID, val subject: String, val body: String, val createdAt: Instant, val isRead: Boolean) {
    init { require(subject.isNotBlank() && body.isNotBlank()) }
}
