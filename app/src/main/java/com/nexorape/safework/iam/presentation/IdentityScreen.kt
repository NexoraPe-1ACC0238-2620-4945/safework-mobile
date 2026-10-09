package com.nexorape.safework.iam.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexorape.safework.R
import com.nexorape.safework.core.designsystem.components.*
import com.nexorape.safework.iam.domain.repositories.IdentityFailure

@Composable
fun IdentityRoute(viewModel: IdentityViewModel, embedded: Boolean = false) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, viewModel) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_START) viewModel.refresh() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    BackHandler(enabled = state.screen == IdentityScreen.REGISTER || state.screen == IdentityScreen.EDIT_PROFILE) {
        viewModel.navigate(if (state.user == null) IdentityScreen.LOGIN else IdentityScreen.PROFILE)
    }
    IdentityContent(state, IdentityActions(viewModel::login, viewModel::register, viewModel::refresh,
        viewModel::saveProfile, viewModel::logout, viewModel::navigate, viewModel::restore), embedded)
}

/** UI callbacks only. The existing ViewModel remains responsible for every IAM operation. */
internal data class IdentityActions(
    val login: (String, String) -> Unit,
    val register: (String, String, String, String) -> Unit,
    val refresh: () -> Unit,
    val saveProfile: (String, String) -> Unit,
    val logout: () -> Unit,
    val navigate: (IdentityScreen) -> Unit,
    val restore: () -> Unit,
)

@Composable
internal fun IdentityContent(state: IdentityUiState, actions: IdentityActions, embedded: Boolean = false) {
    SafeWorkScreen(centerContent = state.screen == IdentityScreen.LOGIN) {
        if (!embedded) SafeWorkBrand()
        val title = when (state.screen) {
            IdentityScreen.LOGIN -> R.string.iam_login_heading
            IdentityScreen.REGISTER -> R.string.iam_register_heading
            IdentityScreen.PROFILE -> R.string.iam_profile
            IdentityScreen.EDIT_PROFILE -> R.string.iam_edit_profile
            IdentityScreen.RESTORING -> R.string.welcome_message
        }
        val subtitle = when (state.screen) {
            IdentityScreen.LOGIN -> R.string.iam_login_subtitle
            IdentityScreen.REGISTER -> R.string.iam_register_subtitle
            IdentityScreen.PROFILE -> R.string.iam_profile_subtitle
            IdentityScreen.EDIT_PROFILE -> R.string.iam_edit_subtitle
            IdentityScreen.RESTORING -> R.string.iam_restoring
        }
        SafeWorkHeading(stringResource(title), stringResource(subtitle))
        if (state.busy) SafeWorkLoading()
        state.error?.let { SafeWorkMessage(stringResource(errorResource(it)), error = true) }
        state.notice?.let {
            SafeWorkMessage(stringResource(when (it) {
                IdentityNotice.REGISTERED -> R.string.iam_registered
                IdentityNotice.PROFILE_SAVED -> R.string.iam_profile_saved
                IdentityNotice.LOGOUT_LOCAL_ONLY -> R.string.iam_logout_local_only
            }), success = it != IdentityNotice.LOGOUT_LOCAL_ONLY)
        }
        when (state.screen) {
            IdentityScreen.RESTORING -> if (!state.busy) {
                SafeWorkSecondaryButton(stringResource(R.string.iam_retry), actions.restore, true, SafeWorkIcons.Refresh)
            }
            IdentityScreen.LOGIN -> LoginForm(state, actions)
            IdentityScreen.REGISTER -> RegisterForm(state, actions)
            IdentityScreen.PROFILE -> Profile(state, actions)
            IdentityScreen.EDIT_PROFILE -> EditProfile(state, actions)
        }
    }
}

@Composable
private fun LoginForm(state: IdentityUiState, actions: IdentityActions) {
    var email by remember(state.emailHint) { mutableStateOf(state.emailHint) }
    var password by remember { mutableStateOf("") }
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        SafeWorkField(email, { email = it }, stringResource(R.string.iam_email), !state.busy, SafeWorkIcons.Email, KeyboardType.Email)
        SafeWorkSecretField(password, { password = it }, stringResource(R.string.iam_password), !state.busy)
        SafeWorkPrimaryButton(stringResource(R.string.iam_login),
            { actions.login(email, password); password = "" }, !state.busy)
    }
    TextButton(onClick = { password = ""; actions.navigate(IdentityScreen.REGISTER) }, enabled = !state.busy,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Text(stringResource(R.string.iam_create_account))
    }
}

@Composable
private fun RegisterForm(state: IdentityUiState, actions: IdentityActions) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var proof by remember { mutableStateOf("") }
    SafeWorkCard {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(SafeWorkIcons.Company, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Text(stringResource(R.string.iam_invitation_help), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        }
        SafeWorkField(name, { name = it }, stringResource(R.string.iam_full_name), !state.busy, SafeWorkIcons.Person)
        SafeWorkField(email, { email = it }, stringResource(R.string.iam_email), !state.busy, SafeWorkIcons.Email, KeyboardType.Email)
        SafeWorkSecretField(password, { password = it }, stringResource(R.string.iam_password), !state.busy)
        Text(stringResource(R.string.iam_password_help), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        SafeWorkSecretField(proof, { proof = it }, stringResource(R.string.iam_invitation), !state.busy, allowReveal = false)
        SafeWorkPrimaryButton(stringResource(R.string.iam_register),
            { actions.register(name, email, password, proof); password = ""; proof = "" }, !state.busy)
    }
    TextButton(onClick = { password = ""; proof = ""; actions.navigate(IdentityScreen.LOGIN) }, enabled = !state.busy,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Text(stringResource(R.string.iam_back_login))
    }
}

@Composable
private fun Profile(state: IdentityUiState, actions: IdentityActions) {
    val user = state.user ?: return
    SafeWorkCard {
        Surface(color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = MaterialTheme.shapes.medium) {
            Icon(SafeWorkIcons.Person, null, Modifier.padding(14.dp).size(28.dp))
        }
        Text(user.fullName.value, style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.semantics { heading() })
        SafeWorkDetail(SafeWorkIcons.Email, stringResource(R.string.iam_email), user.email.value)
        SafeWorkDetail(SafeWorkIcons.Phone, stringResource(R.string.iam_phone_label),
            user.phone?.value ?: stringResource(R.string.iam_not_provided))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Text(stringResource(R.string.iam_workplace_heading), style = MaterialTheme.typography.titleSmall)
        Text(stringResource(R.string.iam_company, user.companyId.value), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.iam_roles, user.roles.joinToString { it.name }),
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SafeWorkPrimaryButton(stringResource(R.string.iam_edit_profile),
            { actions.navigate(IdentityScreen.EDIT_PROFILE) }, !state.busy, SafeWorkIcons.Edit)
        SafeWorkSecondaryButton(stringResource(R.string.iam_refresh_profile), actions.refresh, !state.busy, SafeWorkIcons.Refresh)
    }
    SafeWorkSecondaryButton(stringResource(R.string.iam_logout), actions.logout, !state.busy, SafeWorkIcons.Logout)
    Text(stringResource(R.string.iam_logout_help), style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun EditProfile(state: IdentityUiState, actions: IdentityActions) {
    val user = state.user ?: return
    var name by remember(user) { mutableStateOf(user.fullName.value) }
    var phone by remember(user) { mutableStateOf(user.phone?.value.orEmpty()) }
    SafeWorkCard {
        SafeWorkField(name, { name = it }, stringResource(R.string.iam_full_name), !state.busy, SafeWorkIcons.Person)
        SafeWorkField(phone, { phone = it }, stringResource(R.string.iam_phone), !state.busy, SafeWorkIcons.Phone, KeyboardType.Phone)
        Text(stringResource(R.string.iam_profile_help), style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        SafeWorkPrimaryButton(stringResource(R.string.iam_save), { actions.saveProfile(name, phone) }, !state.busy, SafeWorkIcons.Check)
    }
    TextButton(onClick = { actions.navigate(IdentityScreen.PROFILE) }, enabled = !state.busy,
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text(stringResource(R.string.iam_cancel)) }
}

private fun errorResource(error: IdentityFailure): Int = when (error) {
    IdentityFailure.INVALID_INPUT -> R.string.iam_error_input
    IdentityFailure.INVALID_CREDENTIALS -> R.string.iam_error_credentials
    IdentityFailure.SESSION_INVALID -> R.string.iam_error_session
    IdentityFailure.FORBIDDEN -> R.string.iam_error_forbidden
    IdentityFailure.EMAIL_UNAVAILABLE -> R.string.iam_error_email
    IdentityFailure.INVITATION_INVALID -> R.string.iam_error_invitation
    IdentityFailure.RATE_LIMITED -> R.string.iam_error_rate
    IdentityFailure.NETWORK -> R.string.iam_error_network
    IdentityFailure.STORAGE -> R.string.iam_error_storage
    IdentityFailure.NOT_CONFIGURED -> R.string.iam_error_configuration
    IdentityFailure.SERVER, IdentityFailure.INVALID_RESPONSE -> R.string.iam_error_server
}
