package com.nexorape.safework.notificationmanagement.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexorape.safework.iam.domain.model.UserProfile
import com.nexorape.safework.notificationmanagement.application.GetUserNotifications
import com.nexorape.safework.notificationmanagement.domain.model.UserNotification
import com.nexorape.safework.notificationmanagement.domain.repositories.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class NotificationUiState(val items: List<UserNotification> = emptyList(), val busy: Boolean = false, val error: NotificationFailure? = null)

class NotificationViewModel(private val query: GetUserNotifications, actor: UserProfile, invalidations: StateFlow<Int>) : ViewModel() {
    private val mutable = MutableStateFlow(NotificationUiState())
    val state = mutable.asStateFlow()
    private var identity: UserProfile? = actor
    private var generation = 0
    init {
        viewModelScope.launch { invalidations.drop(1).collect {
            generation++; identity = null; mutable.value = NotificationUiState(error = NotificationFailure.SESSION_INVALID)
        } }
        refresh()
    }
    fun refresh() {
        if (state.value.busy) return
        val actor = identity ?: return
        val revision = generation
        mutable.value = state.value.copy(busy = true, error = null)
        viewModelScope.launch {
            try { val items = query.query(actor); if (generation == revision) mutable.value = NotificationUiState(items) }
            catch (e: CancellationException) { throw e }
            catch (e: NotificationException) { if (generation == revision) mutable.value = state.value.copy(error = e.reason) }
            finally { if (generation == revision) mutable.value = state.value.copy(busy = false) }
        }
    }
}
