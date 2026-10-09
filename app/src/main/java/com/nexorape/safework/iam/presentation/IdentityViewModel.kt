package com.nexorape.safework.iam.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexorape.safework.iam.application.IdentityUseCases
import com.nexorape.safework.iam.domain.model.UserProfile
import com.nexorape.safework.iam.domain.repositories.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class IdentityScreen { RESTORING, LOGIN, REGISTER, PROFILE, EDIT_PROFILE }
enum class IdentityNotice { REGISTERED, PROFILE_SAVED, LOGOUT_LOCAL_ONLY }

data class IdentityUiState(
    val screen: IdentityScreen = IdentityScreen.RESTORING,
    val user: UserProfile? = null,
    val busy: Boolean = false,
    val error: IdentityFailure? = null,
    val notice: IdentityNotice? = null,
    val emailHint: String = "",
)

class IdentityViewModel(private val useCases: IdentityUseCases, invalidations: StateFlow<Int>) : ViewModel() {
    private val mutableState = MutableStateFlow(IdentityUiState())
    val state = mutableState.asStateFlow()
    private var generation = 0
    private var loggingOut = false

    init {
        viewModelScope.launch {
            invalidations.drop(1).collect {
                generation++
                mutableState.value = IdentityUiState(screen = IdentityScreen.LOGIN, busy = state.value.busy,
                    error = if (loggingOut) null else IdentityFailure.SESSION_INVALID)
            }
        }
        restore()
    }

    fun restore() = perform {
        val user = useCases.restore()
        IdentityUiState(screen = if (user == null) IdentityScreen.LOGIN else IdentityScreen.PROFILE, user = user)
    }

    fun login(email: String, password: String) = perform {
        val user = useCases.login(email, password)
        IdentityUiState(screen = IdentityScreen.PROFILE, user = user)
    }

    fun register(name: String, email: String, password: String, invitation: String) = perform {
        val user = useCases.register(name, email, password, invitation)
        IdentityUiState(screen = IdentityScreen.LOGIN, notice = IdentityNotice.REGISTERED, emailHint = user.email.value)
    }

    fun refresh() {
        if (state.value.user == null) return
        perform {
            val user = useCases.profile()
            state.value.copy(user = user, busy = false, error = null)
        }
    }

    fun saveProfile(name: String, phone: String) = perform {
        val user = useCases.updateProfile(name, phone)
        IdentityUiState(screen = IdentityScreen.PROFILE, user = user, notice = IdentityNotice.PROFILE_SAVED)
    }

    fun logout() = perform(isLogout = true) {
        val outcome = useCases.logout()
        IdentityUiState(screen = IdentityScreen.LOGIN,
            notice = if (outcome == LogoutOutcome.LOCAL_ONLY) IdentityNotice.LOGOUT_LOCAL_ONLY else null)
    }

    fun navigate(screen: IdentityScreen) {
        if (state.value.busy) return
        if (screen == IdentityScreen.PROFILE || screen == IdentityScreen.EDIT_PROFILE) {
            if (state.value.user == null) return
        }
        mutableState.value = state.value.copy(screen = screen, error = null, notice = null)
    }

    private fun perform(isLogout: Boolean = false, action: suspend () -> IdentityUiState) {
        if (state.value.busy) return
        mutableState.value = state.value.copy(busy = true, error = null, notice = null)
        val startedGeneration = generation
        loggingOut = isLogout
        viewModelScope.launch {
            try {
                val result = action()
                if (startedGeneration == generation || isLogout) mutableState.value = result
            }
            catch (e: CancellationException) { throw e }
            catch (e: IdentityException) {
                mutableState.value = if (e.reason == IdentityFailure.SESSION_INVALID)
                    IdentityUiState(screen = IdentityScreen.LOGIN, error = e.reason)
                else state.value.copy(busy = false, error = e.reason)
            } finally { loggingOut = false; mutableState.value = state.value.copy(busy = false) }
        }
    }
}
