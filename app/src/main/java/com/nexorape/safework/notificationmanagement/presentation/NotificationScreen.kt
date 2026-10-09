package com.nexorape.safework.notificationmanagement.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexorape.safework.R
import com.nexorape.safework.core.presentation.localTimestamp
import com.nexorape.safework.notificationmanagement.domain.repositories.NotificationFailure

@Composable
fun NotificationRoute(viewModel: NotificationViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.notifications_title), style = MaterialTheme.typography.headlineSmall)
        if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.error?.let { Text(stringResource(notificationError(it)), color = MaterialTheme.colorScheme.error) }
        OutlinedButton(onClick = viewModel::refresh, enabled = !state.busy) { Text(stringResource(R.string.incident_refresh)) }
        if (state.items.isEmpty() && !state.busy) Text(stringResource(R.string.notifications_empty))
        state.items.forEach { notification ->
            OutlinedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(notification.subject, style = MaterialTheme.typography.titleMedium)
                    Text(notification.body)
                    Text(localTimestamp(notification.createdAt), style = MaterialTheme.typography.bodySmall)
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
