package com.nexorape.safework.core.navigation

import androidx.activity.compose.BackHandler

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nexorape.safework.R
import com.nexorape.safework.core.designsystem.components.*
import com.nexorape.safework.core.configuration.AppGraph
import com.nexorape.safework.iam.domain.model.Role
import com.nexorape.safework.iam.presentation.*
import com.nexorape.safework.incidentmanagement.application.IncidentUseCases
import com.nexorape.safework.incidentmanagement.application.CaptureIncidentLocation
import com.nexorape.safework.incidentmanagement.application.IncidentHandlingUseCases
import com.nexorape.safework.incidentmanagement.infrastructure.http.HttpIncidentHandlingRepository
import com.nexorape.safework.incidentmanagement.infrastructure.http.HttpIncidentRepository
import com.nexorape.safework.incidentmanagement.infrastructure.location.DeviceLocation
import com.nexorape.safework.incidentmanagement.presentation.*
import com.nexorape.safework.notificationmanagement.application.GetUserNotifications
import com.nexorape.safework.notificationmanagement.infrastructure.http.HttpNotificationRepository
import com.nexorape.safework.notificationmanagement.presentation.*

private enum class Destination { PROFILE, INCIDENTS, NOTIFICATIONS }

@Composable
fun SafeWorkNavigation(identity: IdentityViewModel, graph: AppGraph) {
    val state by identity.state.collectAsStateWithLifecycle()
    val revision by graph.sessions.invalidations.collectAsStateWithLifecycle()
    val user = state.user
    if (user == null || state.screen != IdentityScreen.PROFILE) { IdentityRoute(identity); return }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner, identity) {
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_START) identity.refresh() }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    var destination by remember(user.id, revision) { mutableStateOf(Destination.PROFILE) }
    BackHandler(enabled = destination != Destination.PROFILE) { destination = Destination.PROFILE }
    val businessUser = Role.WORKER in user.roles || Role.EMPLOYER in user.roles
    val context = LocalContext.current
    val entries = listOfNotNull(
        SafeWorkNavItem(stringResource(R.string.design_nav_profile), SafeWorkIcons.Person),
        if (businessUser) SafeWorkNavItem(stringResource(R.string.incidents_title), SafeWorkIcons.Incidents) else null,
        if (businessUser) SafeWorkNavItem(stringResource(R.string.design_nav_notifications), SafeWorkIcons.Bell) else null,
    )
    SafeWorkShell(entries, if (businessUser) destination.ordinal else 0,
        onSelect = { destination = Destination.entries[it] }) {
            if (destination == Destination.INCIDENTS && businessUser) {
                val incidentModel: IncidentViewModel = viewModel(key = "incident-${user.id.value}-${user.companyId.value}-$revision",
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            require(modelClass == IncidentViewModel::class.java)
                            @Suppress("UNCHECKED_CAST")
                            return IncidentViewModel(IncidentUseCases(HttpIncidentRepository(graph.api)), user,
                                CaptureIncidentLocation(DeviceLocation(context.applicationContext)), graph.sessions.invalidations) as T
                        }
                    })
                IncidentRoute(incidentModel) { incident, busy, changed ->
                    if (Role.EMPLOYER in user.roles) {
                        val handling: IncidentHandlingViewModel = viewModel(key = "handling-${user.id.value}-${incident.id.value}-$revision",
                            factory = object : ViewModelProvider.Factory {
                                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                    require(modelClass == IncidentHandlingViewModel::class.java)
                                    @Suppress("UNCHECKED_CAST")
                                    return IncidentHandlingViewModel(IncidentHandlingUseCases(HttpIncidentHandlingRepository(graph.api)),
                                        IncidentUseCases(HttpIncidentRepository(graph.api)), user, graph.sessions.invalidations) as T
                                }
                            })
                        IncidentHandlingRoute(handling, incident, busy, changed)
                    }
                }
            } else if (destination == Destination.NOTIFICATIONS && businessUser) {
                val notifications: NotificationViewModel = viewModel(key = "notifications-${user.id.value}-${user.companyId.value}-$revision",
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            require(modelClass == NotificationViewModel::class.java)
                            @Suppress("UNCHECKED_CAST")
                            return NotificationViewModel(GetUserNotifications(HttpNotificationRepository(graph.api)), user, graph.sessions.invalidations) as T
                        }
                    })
                NotificationRoute(notifications)
            } else IdentityRoute(identity, embedded = true)
    }
}
