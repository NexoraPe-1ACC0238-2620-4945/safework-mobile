package com.nexorape.safework.iam.presentation

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.stringResource
import com.nexorape.safework.R
import com.nexorape.safework.core.designsystem.components.*
import com.nexorape.safework.core.designsystem.theme.SafeWorkTheme
import com.nexorape.safework.iam.domain.model.*
import com.nexorape.safework.iam.domain.repositories.IdentityFailure

@Preview(name = "Light · English", locale = "en", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Dark · English", locale = "en", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Claro · Español", locale = "es-rPE", widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_NO)
@Preview(name = "Large text · narrow", locale = "en", widthDp = 320, heightDp = 800, fontScale = 1.5f)
@Preview(name = "Tablet", locale = "en", widthDp = 700, heightDp = 900)
private annotation class IdentityVariants

// Synthetic presentation-only samples. No ViewModel, network, sessions or credentials.
private val sampleUser = UserProfile(UserId(1), CompanyId(1), FullName.of("Alex Morgan"),
    EmailAddress.of("alex@example.test"), PhoneNumber.of("+51 900 000 001"), setOf(Role.WORKER))
private val previewActions = IdentityActions({ _, _ -> }, { _, _, _, _ -> }, {}, { _, _ -> }, {}, {}, {})

@IdentityVariants
@Composable
fun LoginPreview() = SafeWorkTheme { IdentityContent(IdentityUiState(screen = IdentityScreen.LOGIN), previewActions) }

@IdentityVariants
@Composable
fun RegistrationPreview() = SafeWorkTheme { IdentityContent(IdentityUiState(screen = IdentityScreen.REGISTER), previewActions) }

@IdentityVariants
@Composable
fun ProfilePreview() = SafeWorkTheme {
    IdentityContent(IdentityUiState(screen = IdentityScreen.PROFILE, user = sampleUser), previewActions)
}

@IdentityVariants
@Composable
fun EditProfilePreview() = SafeWorkTheme {
    IdentityContent(IdentityUiState(screen = IdentityScreen.EDIT_PROFILE, user = sampleUser), previewActions)
}

@IdentityVariants
@Composable
fun LoginErrorPreview() = SafeWorkTheme {
    IdentityContent(IdentityUiState(screen = IdentityScreen.LOGIN, error = IdentityFailure.INVALID_CREDENTIALS), previewActions)
}

@IdentityVariants
@Composable
fun LoginLoadingPreview() = SafeWorkTheme {
    IdentityContent(IdentityUiState(screen = IdentityScreen.LOGIN, busy = true), previewActions)
}

@IdentityVariants
@Composable
fun RestoringPreview() = SafeWorkTheme { IdentityContent(IdentityUiState(busy = true), previewActions) }

@IdentityVariants
@Composable
fun LocalLogoutNoticePreview() = SafeWorkTheme {
    IdentityContent(IdentityUiState(screen = IdentityScreen.LOGIN, notice = IdentityNotice.LOGOUT_LOCAL_ONLY), previewActions)
}

@IdentityVariants
@Composable
fun ProfileWithNavigationPreview() = SafeWorkTheme {
    SafeWorkShell(listOf(
        SafeWorkNavItem(stringResource(R.string.design_nav_profile), SafeWorkIcons.Person),
        SafeWorkNavItem(stringResource(R.string.incidents_title), SafeWorkIcons.Incidents),
        SafeWorkNavItem(stringResource(R.string.design_nav_notifications), SafeWorkIcons.Bell)), 0, {}) {
        IdentityContent(IdentityUiState(screen = IdentityScreen.PROFILE, user = sampleUser), previewActions, embedded = true)
    }
}
