package com.nexorape.safework.iam.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexorape.safework.R
import com.nexorape.safework.iam.domain.repositories.IdentityFailure

@Composable
fun IdentityRoute(viewModel: IdentityViewModel) {
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
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(R.string.welcome_message))
            if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth())
            state.error?.let {
                Text(stringResource(errorResource(it)), color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
            state.notice?.let {
                Text(stringResource(if (it == IdentityNotice.REGISTERED) R.string.iam_registered else R.string.iam_profile_saved),
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
            }
            when (state.screen) {
                IdentityScreen.RESTORING -> {
                    Text(stringResource(R.string.iam_restoring))
                    if (!state.busy) Button(onClick = viewModel::restore) { Text(stringResource(R.string.iam_retry)) }
                }
                IdentityScreen.LOGIN -> LoginForm(state, viewModel)
                IdentityScreen.REGISTER -> RegisterForm(state, viewModel)
                IdentityScreen.PROFILE -> Profile(state, viewModel)
                IdentityScreen.EDIT_PROFILE -> EditProfile(state, viewModel)
            }
        }
    }
}

@Composable
private fun LoginForm(state: IdentityUiState, viewModel: IdentityViewModel) {
    var email by remember(state.emailHint) { mutableStateOf(state.emailHint) }
    var password by remember { mutableStateOf("") }
    Text(stringResource(R.string.iam_login), style = MaterialTheme.typography.headlineSmall)
    Input(email, { email = it }, R.string.iam_email, state.busy, KeyboardType.Email)
    SecretInput(password, { password = it }, R.string.iam_password, state.busy)
    Button(onClick = { viewModel.login(email, password); password = "" }, enabled = !state.busy,
        modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.iam_login)) }
    TextButton(onClick = { password = ""; viewModel.navigate(IdentityScreen.REGISTER) }, enabled = !state.busy) {
        Text(stringResource(R.string.iam_create_account))
    }
}

@Composable
private fun RegisterForm(state: IdentityUiState, viewModel: IdentityViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var proof by remember { mutableStateOf("") }
    Text(stringResource(R.string.iam_create_account), style = MaterialTheme.typography.headlineSmall)
    Text(stringResource(R.string.iam_invitation_help))
    Input(name, { name = it }, R.string.iam_full_name, state.busy)
    Input(email, { email = it }, R.string.iam_email, state.busy, KeyboardType.Email)
    SecretInput(password, { password = it }, R.string.iam_password, state.busy)
    Text(stringResource(R.string.iam_password_help), style = MaterialTheme.typography.bodySmall)
    SecretInput(proof, { proof = it }, R.string.iam_invitation, state.busy)
    Button(onClick = { viewModel.register(name, email, password, proof); password = ""; proof = "" }, enabled = !state.busy,
        modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.iam_register)) }
    TextButton(onClick = { password = ""; proof = ""; viewModel.navigate(IdentityScreen.LOGIN) }, enabled = !state.busy) {
        Text(stringResource(R.string.iam_back_login))
    }
}

@Composable
private fun Profile(state: IdentityUiState, viewModel: IdentityViewModel) {
    val user = state.user ?: return
    Text(stringResource(R.string.iam_profile), style = MaterialTheme.typography.headlineSmall)
    Text(user.fullName.value, style = MaterialTheme.typography.titleLarge)
    Text(user.email.value)
    Text(stringResource(R.string.iam_company, user.companyId.value))
    Text(stringResource(R.string.iam_roles, user.roles.joinToString { it.name }))
    Text(stringResource(R.string.iam_phone_display, user.phone?.value ?: stringResource(R.string.iam_not_provided)))
    Button(onClick = { viewModel.navigate(IdentityScreen.EDIT_PROFILE) }, enabled = !state.busy) {
        Text(stringResource(R.string.iam_edit_profile))
    }
    OutlinedButton(onClick = viewModel::refresh, enabled = !state.busy) { Text(stringResource(R.string.iam_refresh_profile)) }
    Text(stringResource(R.string.iam_logout_help), style = MaterialTheme.typography.bodySmall)
    OutlinedButton(onClick = viewModel::logout, enabled = !state.busy) { Text(stringResource(R.string.iam_logout)) }
}

@Composable
private fun EditProfile(state: IdentityUiState, viewModel: IdentityViewModel) {
    val user = state.user ?: return
    var name by remember(user) { mutableStateOf(user.fullName.value) }
    var phone by remember(user) { mutableStateOf(user.phone?.value.orEmpty()) }
    Text(stringResource(R.string.iam_edit_profile), style = MaterialTheme.typography.headlineSmall)
    Input(name, { name = it }, R.string.iam_full_name, state.busy)
    Input(phone, { phone = it }, R.string.iam_phone, state.busy, KeyboardType.Phone)
    Text(stringResource(R.string.iam_profile_help), style = MaterialTheme.typography.bodySmall)
    Button(onClick = { viewModel.saveProfile(name, phone) }, enabled = !state.busy) { Text(stringResource(R.string.iam_save)) }
    TextButton(onClick = { viewModel.navigate(IdentityScreen.PROFILE) }, enabled = !state.busy) { Text(stringResource(R.string.iam_cancel)) }
}

@Composable
private fun Input(value: String, update: (String) -> Unit, label: Int, busy: Boolean, type: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(value = value, onValueChange = update, label = { Text(stringResource(label)) }, enabled = !busy,
        singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = type), modifier = Modifier.fillMaxWidth())
}

@Composable
private fun SecretInput(value: String, update: (String) -> Unit, label: Int, busy: Boolean) {
    OutlinedTextField(value = value, onValueChange = update, label = { Text(stringResource(label)) }, enabled = !busy,
        singleLine = true, visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, autoCorrectEnabled = false),
        modifier = Modifier.fillMaxWidth())
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
