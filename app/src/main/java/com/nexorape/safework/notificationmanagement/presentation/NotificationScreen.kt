package com.nexorape.safework.notificationmanagement.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexorape.safework.R
import com.nexorape.safework.core.designsystem.components.*
import com.nexorape.safework.core.presentation.localTimestamp
import com.nexorape.safework.notificationmanagement.domain.repositories.NotificationFailure

@Composable
fun NotificationRoute(viewModel: NotificationViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NotificationContent(state, viewModel::refresh)
}

/** Displays server data only: no invented unread counts, read actions or navigation links. */
@Composable
internal fun NotificationContent(state: NotificationUiState, refresh: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(Modifier.widthIn(max = 600.dp).fillMaxWidth(), contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item {
                SafeWorkHeading(stringResource(R.string.notifications_title),
                    stringResource(R.string.design_notifications_caption))
            }
            item {
                SafeWorkSecondaryButton(stringResource(R.string.incident_refresh), refresh, !state.busy, SafeWorkIcons.Refresh)
            }
            if (state.busy) item { SafeWorkLoading() }
            state.error?.let { reason -> item { SafeWorkMessage(stringResource(notificationError(reason)), error = true) } }
            if (state.items.isEmpty() && !state.busy && state.error == null) item {
                SafeWorkCard {
                    Icon(SafeWorkIcons.Bell, null, Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.notifications_empty), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.design_notifications_empty_caption),
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(state.items, key = { it.id.toString() }) { notification ->
                SafeWorkCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.small) {
                            Icon(SafeWorkIcons.Bell, null, Modifier.padding(10.dp).size(20.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Text(notification.subject, style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f).semantics { heading() })
                    }
                    Text(notification.body, style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(SafeWorkIcons.Clock, null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(localTimestamp(notification.createdAt), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun notificationError(reason: NotificationFailure): Int = when (reason) {
    NotificationFailure.SESSION_INVALID -> R.string.iam_error_session
    NotificationFailure.FORBIDDEN -> R.string.iam_error_forbidden
    NotificationFailure.NETWORK -> R.string.iam_error_network
    NotificationFailure.NOT_CONFIGURED -> R.string.iam_error_configuration
    NotificationFailure.STORAGE -> R.string.iam_error_storage
    NotificationFailure.SERVER, NotificationFailure.INVALID_RESPONSE -> R.string.iam_error_server
}
