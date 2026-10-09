package com.nexorape.safework.core.network

import kotlinx.coroutines.flow.StateFlow

class SessionCredential(val token: String, val apiUrl: String) {
    override fun toString() = "SessionCredential([redacted])"
}

class SessionStorageException : RuntimeException("SESSION_STORAGE")

interface SessionStore {
    val invalidations: StateFlow<Int>
    fun read(): SessionCredential?
    fun write(credential: SessionCredential)
    fun clear(expectedToken: String? = null)
}
