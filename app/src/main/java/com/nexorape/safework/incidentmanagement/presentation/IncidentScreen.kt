package com.nexorape.safework.incidentmanagement.presentation

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.nexorape.safework.core.designsystem.components.*
import com.nexorape.safework.R
import com.nexorape.safework.incidentmanagement.domain.model.*
import com.nexorape.safework.incidentmanagement.domain.repositories.*

@Composable
fun IncidentRoute(viewModel: IncidentViewModel,
                  handling: @Composable (Incident, Boolean, () -> Unit) -> Unit = { _, _, _ -> }) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, viewModel) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_STOP) viewModel.stopCapture() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer); viewModel.stopCapture() }
    }
    val permissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) viewModel.captureLocation() else viewModel.denyLocation()
    }
    BackHandler(enabled = state.page != IncidentPage.LIST) { viewModel.back() }
    SafeWorkScreen {

        if (state.busy || state.locating) SafeWorkLoading()
        state.error?.let {
            val resource = if (state.page == IncidentPage.DOCUMENT && it == IncidentFailure.INVALID_INPUT)
                R.string.incident_document_invalid else incidentError(it)
            SafeWorkMessage(stringResource(resource), error = true)
        }
        when (state.page) {
            IncidentPage.LIST -> {
                SafeWorkHeading(stringResource(R.string.incidents_title), stringResource(R.string.design_incidents_caption))
                SafeWorkPrimaryButton(stringResource(R.string.incident_report), viewModel::openReport, !state.busy, SafeWorkIcons.Incidents)
                SafeWorkSecondaryButton(stringResource(R.string.incident_refresh), viewModel::refresh, !state.busy, SafeWorkIcons.Refresh)
                IncidentFilters(state, viewModel::filterStatus, viewModel::filterReporter, viewModel::clearFilters)
                if (state.items.isEmpty() && !state.busy && state.error == null) Text(stringResource(R.string.incidents_empty))
                if (state.items.isNotEmpty() && !state.busy && state.error == null) {
                    Text(pluralStringResource(R.plurals.incident_filter_count, state.items.size,
                        state.visibleItems.size, state.items.size),
                        style = MaterialTheme.typography.bodySmall)
                    if (state.visibleItems.isEmpty()) Text(stringResource(R.string.incident_filter_empty))
                }
                state.visibleItems.forEach { incident ->
                    OutlinedCard(onClick = { viewModel.detail(incident.id) }, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            IncidentStatusLabel(incident.status)
                            Text(incident.title.value, style = MaterialTheme.typography.titleMedium)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(SafeWorkIcons.Pin, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(incident.location.value, style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            IncidentPage.DETAIL -> state.selected?.let { incident ->
                Text(incident.title.value, style = MaterialTheme.typography.headlineSmall)
                IncidentStatusLabel(incident.status)
                if (state.reported) SafeWorkMessage(stringResource(R.string.incident_reported), success = true)
                if (state.documentSaved) SafeWorkMessage(stringResource(R.string.incident_document_saved), success = true)
                SafeWorkCard {
                    Text(incident.description.value, style = MaterialTheme.typography.bodyLarge)
                    SafeWorkDetail(SafeWorkIcons.Pin, stringResource(R.string.incident_location), incident.location.value)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text(stringResource(R.string.incident_reporter, incident.reporterName), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.incident_responsible, incident.assigneeName ?: stringResource(R.string.incident_unassigned)),
                        style = MaterialTheme.typography.bodyMedium)
                }
                IncidentReferences(incident, !state.busy)
                if (viewModel.canEditDocument()) {
                    SafeWorkSecondaryButton(stringResource(R.string.incident_document_edit), viewModel::openDocument, !state.busy, SafeWorkIcons.Edit)
                }
                handling(incident, state.busy) { viewModel.detail(incident.id) }
                SafeWorkSecondaryButton(stringResource(R.string.incident_refresh), { viewModel.detail(incident.id) }, !state.busy, SafeWorkIcons.Refresh)
                TextButton(onClick = viewModel::back, enabled = !state.busy) { Text(stringResource(R.string.incident_back)) }
            }
            IncidentPage.REPORT -> {
                Text(stringResource(R.string.incident_report), style = MaterialTheme.typography.headlineSmall)
                OutlinedTextField(state.title, { viewModel.edit(title = it) }, label = { Text(stringResource(R.string.incident_title)) },
                    enabled = !state.busy, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(state.description, { viewModel.edit(description = it) }, label = { Text(stringResource(R.string.incident_description)) },
                    enabled = !state.busy, minLines = 3, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(state.location, { viewModel.edit(location = it) }, label = { Text(stringResource(R.string.incident_location)) },
                    enabled = !state.busy, modifier = Modifier.fillMaxWidth())
                Text(stringResource(R.string.incident_limits), style = MaterialTheme.typography.bodySmall)
                Text(stringResource(R.string.incident_location_help))
                if (state.locationUnavailable) Text(stringResource(R.string.incident_location_unavailable))
                OutlinedButton(onClick = {
                    if (context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) viewModel.captureLocation()
                    else permissions.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                }, enabled = !state.busy && !state.locating) { Text(stringResource(R.string.incident_capture_location)) }
                SafeWorkPrimaryButton(stringResource(R.string.incident_submit), viewModel::report, !state.busy && !state.locating)
                TextButton(onClick = viewModel::back, enabled = !state.busy) { Text(stringResource(R.string.iam_cancel)) }
            }
            IncidentPage.DOCUMENT -> {
                Text(stringResource(R.string.incident_document_edit), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(R.string.incident_document_help))
                OutlinedTextField(state.documentUrl, viewModel::editDocument,
                    label = { Text(stringResource(R.string.incident_document_url)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    enabled = !state.busy, modifier = Modifier.fillMaxWidth())
                SafeWorkPrimaryButton(stringResource(R.string.incident_document_save), viewModel::saveDocument,
                    !state.busy && state.documentUrl.isNotBlank())
                TextButton(onClick = viewModel::back, enabled = !state.busy) { Text(stringResource(R.string.iam_cancel)) }
            }
        }
    }
}

fun statusResource(status: IncidentStatus): Int = when (status) {
    IncidentStatus.OPEN -> R.string.incident_open
    IncidentStatus.ASSIGNED -> R.string.incident_assigned
    IncidentStatus.IN_PROGRESS -> R.string.incident_in_progress
    IncidentStatus.CLOSED -> R.string.incident_closed
}

fun incidentError(error: IncidentFailure): Int = when (error) {
    IncidentFailure.INVALID_INPUT -> R.string.incident_error_input
    IncidentFailure.INVALID_DOCUMENT_URL -> R.string.incident_document_invalid
    IncidentFailure.STATE_CONFLICT -> R.string.incident_error_conflict
    IncidentFailure.NOT_FOUND -> R.string.incident_error_not_found
    IncidentFailure.SESSION_INVALID -> R.string.iam_error_session
    IncidentFailure.FORBIDDEN -> R.string.iam_error_forbidden
    IncidentFailure.NETWORK -> R.string.iam_error_network
    IncidentFailure.NOT_CONFIGURED -> R.string.iam_error_configuration
    IncidentFailure.STORAGE -> R.string.iam_error_storage
    IncidentFailure.SERVER, IncidentFailure.INVALID_RESPONSE -> R.string.iam_error_server
}

@Composable
private fun IncidentFilters(state: IncidentUiState, statusChanged: (IncidentStatus?) -> Unit,
                            reporterChanged: (Boolean) -> Unit, clear: () -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(selected = state.reporterFilter == null, onClick = { reporterChanged(false) },
            label = { Text(stringResource(R.string.incident_filter_company)) }, enabled = !state.busy,
            modifier = Modifier.weight(1f))
        FilterChip(selected = state.reporterFilter != null, onClick = { reporterChanged(true) },
            label = { Text(stringResource(R.string.incident_filter_mine)) }, enabled = !state.busy,
            modifier = Modifier.weight(1f))
    }
    var expanded by remember { mutableStateOf(false) }
    val selectedStatus = state.statusFilter?.let { stringResource(statusResource(it)) }
        ?: stringResource(R.string.incident_filter_all_statuses)
    Box {
        OutlinedButton(onClick = { expanded = true }, enabled = !state.busy) {
            Text(stringResource(R.string.incident_filter_status, selectedStatus))
        }
        DropdownMenu(expanded = expanded && !state.busy, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text(stringResource(R.string.incident_filter_all_statuses)) },
                onClick = { statusChanged(null); expanded = false })
            IncidentStatus.entries.forEach { status ->
                DropdownMenuItem(text = { Text(stringResource(statusResource(status))) },
                    onClick = { statusChanged(status); expanded = false })
            }
        }
    }
    if (state.statusFilter != null || state.reporterFilter != null) {
        TextButton(onClick = clear, enabled = !state.busy) { Text(stringResource(R.string.incident_filter_clear)) }
    }
}

@Composable
private fun IncidentReferences(incident: Incident, enabled: Boolean) {
    val context = LocalContext.current
    var failed by remember(incident.id, incident.documentUrl) { mutableStateOf(false) }
    if (failed) SafeWorkMessage(stringResource(R.string.incident_reference_unavailable), error = true)
    SafeWorkSecondaryButton(stringResource(R.string.incident_open_map), {
        val url = "https://www.google.com/maps/search/?api=1&query=${Uri.encode(incident.location.value)}"
        failed = !openHttpsReference(context, url)
    }, enabled, SafeWorkIcons.Pin)
    incident.documentUrl?.let { raw ->
        val valid = runCatching { IncidentDocumentUrl.of(raw) }.isSuccess
        if (valid) {
            SafeWorkSecondaryButton(stringResource(R.string.incident_document_open), {
                failed = !openHttpsReference(context, raw)
            }, enabled, SafeWorkIcons.Arrow)
        } else {
            Text(stringResource(R.string.incident_document_unavailable), style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun openHttpsReference(context: Context, rawUrl: String): Boolean {
    val url = runCatching { java.net.URI(rawUrl) }.getOrNull() ?: return false
    if (url.scheme != "https" || url.host.isNullOrBlank() || url.rawUserInfo != null) return false
    return try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(rawUrl))
            .addCategory(Intent.CATEGORY_BROWSABLE).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        true
    } catch (_: ActivityNotFoundException) { false }
    catch (_: SecurityException) { false }
}

@Composable
private fun IncidentStatusLabel(status: IncidentStatus) {
    val colors = MaterialTheme.colorScheme
    Surface(color = if (status == IncidentStatus.CLOSED) colors.tertiaryContainer else colors.primaryContainer,
        contentColor = if (status == IncidentStatus.CLOSED) colors.onTertiaryContainer else colors.onPrimaryContainer,
        shape = MaterialTheme.shapes.small) {
        Text(stringResource(statusResource(status)), style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
    }
}
