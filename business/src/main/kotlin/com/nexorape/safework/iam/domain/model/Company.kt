package com.nexorape.safework.iam.domain.model

@JvmInline
value class CompanyName private constructor(val value: String) {
    companion object {
        fun of(raw: String): CompanyName {
            val value = raw.trim { it.isWhitespace() || Character.isSpaceChar(it) }
            require(value.isNotBlank() && value.length <= 120)
            return CompanyName(value)
        }
    }
}

data class CompanySummary(val id: CompanyId, val name: CompanyName)
enum class RequestStatus { PENDING, ACCEPTED, REJECTED, CANCELLED }

data class MembershipRequest(
    val id: Long,
    val companyId: CompanyId,
    val userId: UserId,
    val fullName: FullName,
    val email: EmailAddress,
    val status: RequestStatus,
) {
    init { require(id > 0) }
}
