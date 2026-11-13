package com.nexorape.safework.incidentmanagement.domain.model

import java.net.URI

/** A reference to an existing HTTPS document; this does not upload a file. */
@JvmInline
value class IncidentDocumentUrl private constructor(val value: String) {
    companion object {
        fun of(raw: String): IncidentDocumentUrl {
            val value = raw.trim()
            require(value.length in 1..2048 && value.none { it.isWhitespace() || it.isISOControl() })
            val uri = try { URI(value) } catch (_: Exception) { throw IllegalArgumentException("Invalid document reference") }
            require(uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.rawUserInfo == null)
            require(uri.port == -1 || uri.port in 1..65535)
            return IncidentDocumentUrl(value)
        }
    }
}
