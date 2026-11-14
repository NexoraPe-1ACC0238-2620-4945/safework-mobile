package com.nexorape.safework.notificationmanagement.application

import com.nexorape.safework.iam.domain.model.*
import com.nexorape.safework.notificationmanagement.domain.repositories.*

class GetUserNotifications(private val repository: NotificationRepository) {
    suspend fun query(actor: UserProfile) = if (actor.canAccessCompany)
        repository.ownNotifications().sortedByDescending { it.createdAt }
    else throw NotificationException(NotificationFailure.FORBIDDEN)
}
