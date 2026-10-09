package com.nexorape.safework.core.navigation

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
import com.nexorape.safework.core.configuration.AppGraph
import com.nexorape.safework.iam.domain.model.Role
import com.nexorape.safework.iam.presentation.*
import com.nexorape.safework.incidentmanagement.application.IncidentUseCases
import com.nexorape.safework.incidentmanagement.application.CaptureIncidentLocation
import com.nexorape.safework.incidentmanagement.infrastructure.http.HttpIncidentRepository
import com.nexorape.safework.incidentmanagement.infrastructure.location.DeviceLocation
import com.nexorape.safework.incidentmanagement.presentation.*

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
    var incidents by remember(user.id, revision) { mutableStateOf(false) }
    val businessUser = Role.WORKER in user.roles || Role.EMPLOYER in user.roles
    val context = LocalContext.current
    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Row {
                TextButton(onClick = { incidents = false }) { Text(stringResource(R.string.iam_profile)) }
                if (businessUser) TextButton(onClick = { incidents = true }) { Text(stringResource(R.string.incidents_title)) }
            }
            if (incidents && businessUser) {
                val incidentModel: IncidentViewModel = viewModel(key = "incident-${user.id.value}-${user.companyId.value}-$revision",
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            require(modelClass == IncidentViewModel::class.java)
                            @Suppress("UNCHECKED_CAST")
                            return IncidentViewModel(IncidentUseCases(HttpIncidentRepository(graph.api)), user,
                                CaptureIncidentLocation(DeviceLocation(context.applicationContext)), graph.sessions.invalidations) as T
                        }
                    })
                IncidentRoute(incidentModel)
            } else IdentityRoute(identity)
        }
    }
}
