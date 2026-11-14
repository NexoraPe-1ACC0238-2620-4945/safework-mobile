package com.nexorape.safework.iam.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexorape.safework.R
import com.nexorape.safework.core.designsystem.components.*
import com.nexorape.safework.iam.domain.model.*

@Composable
fun CompanyRoute(model: CompanyViewModel, actor: UserProfile, refreshIdentity: () -> Unit, signInAgain: () -> Unit) {
    val state by model.state.collectAsStateWithLifecycle()
    CompanyContent(state, actor, CompanyActions(
        refresh = { refreshIdentity(); model.refresh() }, search = { model.search(it) },
        moreCompanies = { model.search(state.search, next = true) }, moreRequests = model::moreRequests,
        moreMembers = model::moreMembers,
        create = model::create, request = model::request, decide = model::decide, revoke = model::revoke, signInAgain = signInAgain))
}

internal data class CompanyActions(
    val refresh: () -> Unit,
    val search: (String) -> Unit,
    val moreCompanies: () -> Unit,
    val moreRequests: () -> Unit,
    val moreMembers: () -> Unit,
    val create: (String) -> Unit,
    val request: (CompanySummary) -> Unit,
    val decide: (MembershipRequest, Boolean) -> Unit,
    val revoke: (UserProfile) -> Unit,
    val signInAgain: () -> Unit,
)

@Composable
internal fun CompanyContent(state: CompanyUiState, actor: UserProfile, actions: CompanyActions) {
    var selectedCompany by remember { mutableStateOf<CompanySummary?>(null) }
    var revokedMember by remember { mutableStateOf<UserProfile?>(null) }
    var creating by remember { mutableStateOf(false) }
    SafeWorkScreen {
        SafeWorkHeading(stringResource(R.string.company_title), stringResource(when {
            actor.isEmployer -> R.string.company_employer_help
            actor.hasActiveMembership -> R.string.company_worker_help
            actor.membershipStatus == MembershipStatus.REVOKED -> R.string.company_revoked_help
            else -> R.string.company_onboarding_help
        }))
        if (state.busy) SafeWorkLoading()
        state.error?.let { SafeWorkMessage(stringResource(errorResource(it)), error = true) }
        state.notice?.let { notice -> SafeWorkMessage(stringResource(when (notice) {
            CompanyNotice.REQUEST_SENT -> R.string.company_request_sent
            CompanyNotice.ACCEPTED -> R.string.company_accepted_notice
            CompanyNotice.REJECTED -> R.string.company_rejected_notice
            CompanyNotice.REVOKED -> R.string.company_revoked_notice
        }), success = true) }
        if (state.created != null) {
            SafeWorkCard {
                Text(state.created.name.value, style = MaterialTheme.typography.titleLarge)
                SafeWorkMessage(stringResource(R.string.company_created_login), success = true)
                SafeWorkPrimaryButton(stringResource(R.string.iam_login), actions.signInAgain, !state.busy)
            }
            return@SafeWorkScreen
        }
        state.company?.let { company ->
            SafeWorkCard {
                SafeWorkDetail(SafeWorkIcons.Company, stringResource(R.string.company_title), company.name.value)
                Text(stringResource(membershipResource(actor.membershipStatus)), style = MaterialTheme.typography.bodyMedium)
            }
        }
        SafeWorkSecondaryButton(stringResource(R.string.company_refresh), actions.refresh, !state.busy, SafeWorkIcons.Refresh)
        if (!state.loaded) return@SafeWorkScreen
        val pending = state.requests.any { it.status == RequestStatus.PENDING }
        if (!actor.hasActiveMembership) {
            if (pending) SafeWorkMessage(stringResource(R.string.company_pending_help))
            if (actor.companyId == null) {
                SafeWorkTabs(listOf(stringResource(R.string.company_join), stringResource(R.string.company_create)),
                    if (creating) 1 else 0, onSelect = { if (!state.busy) creating = it == 1 })
                if (creating) CreateCompany(state, actions)
                else if (!pending) FindCompany(state, actions, enabled = !state.busy, select = { selectedCompany = it })
            } else if (state.company != null) {
                SafeWorkPrimaryButton(stringResource(R.string.company_request_again),
                    { selectedCompany = state.company }, !state.busy && !pending)
            }
        }
        Text(stringResource(if (actor.isEmployer) R.string.company_requests else R.string.company_own_requests),
            style = MaterialTheme.typography.titleLarge)
        if (state.requests.isEmpty()) Text(stringResource(R.string.company_no_requests))
        state.requests.forEach { request ->
            SafeWorkCard {
                if (actor.isEmployer) {
                    Text(request.fullName.value, style = MaterialTheme.typography.titleMedium)
                    Text(request.email.value, style = MaterialTheme.typography.bodyMedium)
                } else {
                    Text(state.companies.find { it.id == request.companyId }?.name?.value
                        ?: state.company?.takeIf { it.id == request.companyId }?.name?.value
                        ?: stringResource(R.string.iam_company, request.companyId.value))
                }
                Text(stringResource(requestStatusResource(request.status)))
                if (actor.isEmployer && request.status == RequestStatus.PENDING) {
                    SafeWorkPrimaryButton(stringResource(R.string.company_accept), { actions.decide(request, true) }, !state.busy, SafeWorkIcons.Check)
                    SafeWorkSecondaryButton(stringResource(R.string.company_reject), { actions.decide(request, false) }, !state.busy, SafeWorkIcons.Info)
                }
            }
        }
        if (state.moreRequests) TextButton(onClick = actions.moreRequests, enabled = !state.busy) { Text(stringResource(R.string.company_more)) }
        if (actor.isEmployer) {
            Text(stringResource(R.string.company_members), style = MaterialTheme.typography.titleLarge)
            if (state.members.isEmpty()) Text(stringResource(R.string.company_no_members))
            state.members.forEach { member ->
                SafeWorkCard {
                    Text(member.fullName.value, style = MaterialTheme.typography.titleMedium)
                    Text(member.email.value, style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(membershipResource(member.membershipStatus)))
                    Text(stringResource(if (Role.EMPLOYER in member.roles) R.string.company_role_employer else R.string.company_role_worker))
                    if (member.id != actor.id && Role.EMPLOYER !in member.roles && member.hasActiveMembership) {
                        SafeWorkSecondaryButton(stringResource(R.string.company_revoke), { revokedMember = member }, !state.busy, SafeWorkIcons.Info)
                    }
                }
            }
            if (state.moreMembers) TextButton(onClick = actions.moreMembers, enabled = !state.busy) { Text(stringResource(R.string.company_more)) }
        }
    }
    selectedCompany?.let { company ->
        AlertDialog(onDismissRequest = { selectedCompany = null },
            title = { Text(stringResource(R.string.company_request_title)) },
            text = { Text(stringResource(R.string.company_request_confirmation, company.name.value)) },
            confirmButton = { TextButton(onClick = { selectedCompany = null; actions.request(company) }, enabled = !state.busy) {
                Text(stringResource(R.string.company_request)) } },
            dismissButton = { TextButton(onClick = { selectedCompany = null }) { Text(stringResource(R.string.iam_cancel)) } })
    }
    revokedMember?.let { member ->
        AlertDialog(onDismissRequest = { revokedMember = null },
            title = { Text(stringResource(R.string.company_revoke)) },
            text = { Text(stringResource(R.string.company_revoke_confirmation, member.fullName.value, member.email.value)) },
            confirmButton = { TextButton(onClick = { revokedMember = null; actions.revoke(member) }, enabled = !state.busy) {
                Text(stringResource(R.string.company_revoke)) } },
            dismissButton = { TextButton(onClick = { revokedMember = null }) { Text(stringResource(R.string.iam_cancel)) } })
    }
}

@Composable
private fun FindCompany(state: CompanyUiState, actions: CompanyActions, enabled: Boolean, select: (CompanySummary) -> Unit) {
    var query by remember { mutableStateOf(state.search) }
    SafeWorkCard {
        Text(stringResource(R.string.company_find), style = MaterialTheme.typography.titleMedium)
        SafeWorkField(query, { query = it.take(120) }, stringResource(R.string.company_search_label), !state.busy, SafeWorkIcons.Company)
        SafeWorkSecondaryButton(stringResource(R.string.company_search), { actions.search(query) }, !state.busy, SafeWorkIcons.Company)
        if (state.companies.isEmpty()) Text(stringResource(R.string.company_no_results))
        state.companies.forEach { company ->
            HorizontalDivider()
            Text(company.name.value, style = MaterialTheme.typography.titleSmall)
            Text(stringResource(R.string.iam_company, company.id.value), style = MaterialTheme.typography.bodySmall)
            SafeWorkPrimaryButton(stringResource(R.string.company_request), { select(company) }, enabled)
        }
        if (state.moreCompanies) TextButton(onClick = actions.moreCompanies, enabled = !state.busy) { Text(stringResource(R.string.company_more)) }
    }
}

@Composable
private fun CreateCompany(state: CompanyUiState, actions: CompanyActions) {
    var name by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf(false) }
    SafeWorkCard {
        Text(stringResource(R.string.company_create), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.company_create_help), style = MaterialTheme.typography.bodyMedium)
        SafeWorkField(name, { name = it }, stringResource(R.string.company_name), !state.busy, SafeWorkIcons.Company)
        SafeWorkPrimaryButton(stringResource(R.string.company_create), { confirm = true }, !state.busy && name.isNotBlank())
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false },
        title = { Text(stringResource(R.string.company_create)) },
        text = { Text(stringResource(R.string.company_create_confirmation, name.trim())) },
        confirmButton = { TextButton(onClick = { confirm = false; actions.create(name) }, enabled = !state.busy) { Text(stringResource(R.string.company_create)) } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.iam_cancel)) } })
}

internal fun requestStatusResource(status: RequestStatus) = when (status) {
    RequestStatus.PENDING -> R.string.company_status_pending
    RequestStatus.ACCEPTED -> R.string.company_status_accepted
    RequestStatus.REJECTED -> R.string.company_status_rejected
    RequestStatus.CANCELLED -> R.string.company_status_cancelled
}
