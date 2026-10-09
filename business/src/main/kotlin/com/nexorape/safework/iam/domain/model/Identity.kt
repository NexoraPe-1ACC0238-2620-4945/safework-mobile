package com.nexorape.safework.iam.domain.model

import java.util.Locale

@JvmInline
value class UserId(val value: Long) { init { require(value > 0) } }

@JvmInline
value class CompanyId(val value: Long) { init { require(value > 0) } }

@JvmInline
value class EmailAddress private constructor(val value: String) {
    companion object {
        fun of(raw: String): EmailAddress {
            val value = raw.trim().lowercase(Locale.ROOT)
            require(value.length <= 254 && Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(value))
            return EmailAddress(value)
        }
    }
}

@JvmInline
value class FullName private constructor(val value: String) {
    companion object {
        fun of(raw: String): FullName {
            val value = raw.trim { it.isWhitespace() || Character.isSpaceChar(it) }
            require(value.isNotBlank() && value.codePointCount(0, value.length) <= 120)
            require(wellFormed(value))
            return FullName(value)
        }
    }
}

class Password private constructor(val value: String) {
    override fun toString() = "Password([redacted])"
    companion object {
        fun forRegistration(raw: String): Password {
            require(wellFormed(raw) && raw.codePointCount(0, raw.length) in 12..64 && raw.toByteArray(Charsets.UTF_8).size <= 64)
            return Password(raw)
        }
        fun forLogin(raw: String): Password {
            require(wellFormed(raw) && raw.isNotEmpty() && raw.toByteArray(Charsets.UTF_8).size <= 64)
            return Password(raw)
        }
    }
}

class InvitationProof private constructor(val value: String) {
    override fun toString() = "InvitationProof([redacted])"
    companion object {
        fun of(raw: String): InvitationProof {
            val value = raw.trim()
            require(value.isNotEmpty() && value.length <= 2048)
            return InvitationProof(value)
        }
    }
}

@JvmInline
value class PhoneNumber private constructor(val value: String) {
    companion object {
        fun of(raw: String): PhoneNumber {
            val value = raw.trim()
            require(value.length in 7..32 && value.any(Char::isDigit))
            require(Regex("^\\+?[0-9() .-]+$").matches(value))
            return PhoneNumber(value)
        }
    }
}

enum class Role { WORKER, EMPLOYER, ADMIN }

data class UserProfile(
    val id: UserId,
    val companyId: CompanyId,
    val fullName: FullName,
    val email: EmailAddress,
    val phone: PhoneNumber?,
    val roles: Set<Role>,
)

private fun wellFormed(value: String): Boolean {
    var index = 0
    while (index < value.length) {
        val unit = value[index++]
        if (Character.isHighSurrogate(unit)) {
            if (index >= value.length || !Character.isLowSurrogate(value[index++])) return false
        } else if (Character.isLowSurrogate(unit)) return false
    }
    return true
}
