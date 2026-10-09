package com.nexorape.safework.incidentmanagement.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexorape.safework.R
import com.nexorape.safework.core.presentation.localTimestamp
import com.nexorape.safework.incidentmanagement.domain.model.*

@Composable
fun IncidentHandlingRoute(viewModel: IncidentHandlingViewModel, incident: Incident, externalBusy: Boolean, changed: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(incident) { viewModel.observe(incident) }
    DisposableEffect(viewModel) { onDispose { viewModel.hide() } }
    var confirmClose by remember(incident.id) { mutableStateOf(false) }
    val busy = externalBusy || state.busy
    val current = state.incident ?: incident
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.error?.let {
            Text(stringResource(incidentError(it)), color = MaterialTheme.colorScheme.error)
            TextButton(onClick = { viewModel.observe(incident) }, enabled = !busy) { Text(stringResource(R.string.incident_refresh)) }
        }
        when {
            current.status == IncidentStatus.OPEN && state.assignment == null ->
                Button(onClick = { viewModel.take(changed) }, enabled = !busy && state.error == null) { Text(stringResource(R.string.handling_take)) }
            current.status == IncidentStatus.ASSIGNED && state.assignment != null ->
                Button(onClick = { viewModel.start(changed) }, enabled = !busy && state.error == null) { Text(stringResource(R.string.handling_start)) }
            current.status == IncidentStatus.IN_PROGRESS && state.assignment != null ->
                Button(onClick = { confirmClose = true }, enabled = !busy && state.error == null) { Text(stringResource(R.string.handling_close)) }
            current.status != IncidentStatus.CLOSED && !busy -> Text(stringResource(R.string.handling_other_responsible))
        }
        state.assignment?.let { assignment ->
            Text(stringResource(R.string.handling_assigned_at, localTimestamp(assignment.assignedAt)))
            assignment.completionDate?.let { Text(stringResource(R.string.handling_closed_at, localTimestamp(it))) }
        }
    }
    if (confirmClose) AlertDialog(onDismissRequest = { confirmClose = false },
        title = { Text(stringResource(R.string.handling_close)) }, text = { Text(stringResource(R.string.handling_close_confirmation)) },
        confirmButton = { TextButton(onClick = { confirmClose = false; viewModel.close(changed) }, enabled = !busy) { Text(stringResource(R.string.handling_close)) } },
        dismissButton = { TextButton(onClick = { confirmClose = false }) { Text(stringResource(R.string.iam_cancel)) } })
}
