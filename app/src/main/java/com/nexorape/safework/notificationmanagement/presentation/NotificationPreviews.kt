package com.nexorape.safework.notificationmanagement.presentation

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.nexorape.safework.R
import com.nexorape.safework.core.designsystem.components.*
import com.nexorape.safework.core.designsystem.theme.SafeWorkTheme
import com.nexorape.safework.notificationmanagement.domain.model.UserNotification
import com.nexorape.safework.notificationmanagement.domain.repositories.NotificationFailure
import java.time.Instant
import java.util.UUID

@Preview(name = "Light - English", widthDp = 360, heightDp = 800, locale = "en", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark - English", widthDp = 360, heightDp = 800, locale = "en", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Spanish - large text", widthDp = 320, heightDp = 800, locale = "es-rPE", fontScale = 1.5f)
private annotation class NotificationVariants

// Synthetic presentation fixtures; no ViewModel/session, backend request or device coordinates.
private val sampleNotifications = listOf(
    UserNotification(UUID.fromString("00000000-0000-0000-0000-000000000001"), "Incident assigned",
        "You took responsibility for the synthetic safety report.", Instant.parse("2026-10-09T06:35:00Z"), false),
    UserNotification(UUID.fromString("00000000-0000-0000-0000-000000000002"), "Incident closed",
        "The synthetic report has been resolved.", Instant.parse("2026-10-09T06:36:00Z"), false),
)

@Composable
private fun NotificationPreviewShell(state: NotificationUiState) = SafeWorkTheme {
    SafeWorkShell(listOf(
        SafeWorkNavItem(stringResource(R.string.design_nav_profile), SafeWorkIcons.Person),
        SafeWorkNavItem(stringResource(R.string.incidents_title), SafeWorkIcons.Incidents),
        SafeWorkNavItem(stringResource(R.string.design_nav_notifications), SafeWorkIcons.Bell)), 2, {}) {
        NotificationContent(state, {})
    }
}

@NotificationVariants
@Composable
fun NotificationsPreview() = NotificationPreviewShell(NotificationUiState(items = sampleNotifications))

@NotificationVariants
@Composable
fun NotificationsEmptyPreview() = NotificationPreviewShell(NotificationUiState())

@NotificationVariants
@Composable
fun NotificationsErrorPreview() = NotificationPreviewShell(NotificationUiState(error = NotificationFailure.SERVER))

@NotificationVariants
@Composable
fun NotificationsLoadingPreview() = NotificationPreviewShell(NotificationUiState(busy = true))
