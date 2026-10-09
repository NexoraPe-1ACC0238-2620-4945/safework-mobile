package com.nexorape.safework

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.nexorape.safework.core.designsystem.theme.SafeWorkTheme
import com.nexorape.safework.iam.presentation.IdentityRoute
import com.nexorape.safework.iam.presentation.IdentityViewModel

class MainActivity : ComponentActivity() {
    private val graph get() = (application as SafeWorkApplication).graph
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val identity = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass == IdentityViewModel::class.java)
                @Suppress("UNCHECKED_CAST")
                return IdentityViewModel(graph.identity, graph.sessions.invalidations) as T
            }
        })[IdentityViewModel::class.java]
        setContent { SafeWorkTheme { IdentityRoute(identity) } }
    }
}
