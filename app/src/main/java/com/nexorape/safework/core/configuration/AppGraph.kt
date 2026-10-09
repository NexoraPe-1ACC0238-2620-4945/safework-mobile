package com.nexorape.safework.core.configuration

import android.content.Context
import com.nexorape.safework.BuildConfig
import com.nexorape.safework.core.network.ApiClient
import com.nexorape.safework.iam.application.IdentityUseCases
import com.nexorape.safework.iam.infrastructure.http.HttpIdentityRepository
import com.nexorape.safework.iam.infrastructure.persistence.EncryptedSessionStore

class AppGraph(context: Context) {
    val sessions = EncryptedSessionStore(context.applicationContext)
    val api = ApiClient(BuildConfig.API_URL, BuildConfig.DEBUG, sessions)
    val identity = IdentityUseCases(HttpIdentityRepository(api, sessions))
}
