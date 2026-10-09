package com.nexorape.safework.iam

import com.nexorape.safework.iam.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class IdentityRulesTest {
    @Test fun normalizesIdentityWithoutNormalizingPassword() {
        assertEquals("synthetic@example.test", EmailAddress.of(" Synthetic@Example.Test ").value)
        assertEquals("Synthetic Worker", FullName.of(" Synthetic Worker ").value)
        assertEquals("  Synthetic1  ", Password.forRegistration("  Synthetic1  ").value)
        assertFalse(Password.forLogin("old-password").toString().contains("old-password"))
        assertFalse(InvitationProof.of("synthetic-proof").toString().contains("synthetic-proof"))
    }

    @Test fun enforcesUnicodeAndUtf8Limits() {
        assertEquals(120, FullName.of("😀".repeat(120)).value.codePointCount(0, 240))
        invalid { FullName.of("😀".repeat(121)) }
        Password.forRegistration("😀".repeat(16))
        invalid { Password.forRegistration("😀".repeat(17)) }
        invalid { Password.forRegistration("short") }
        Password.forLogin("legacy")
        invalid { Password.forLogin("") }
    }

    @Test fun rejectsMalformedIdentityAndPhone() {
        listOf("bad", "a@@example.test", "a b@example.test").forEach { invalid { EmailAddress.of(it) } }
        invalid { FullName.of(" \n ") }
        PhoneNumber.of("+51 900 000 000")
        listOf("123", "       ", "51+900000000", "+-------", "letters").forEach { invalid { PhoneNumber.of(it) } }
        invalid { InvitationProof.of(" ") }
    }

    private fun invalid(action: () -> Unit) {
        try { action(); fail("Invalid value accepted") } catch (_: IllegalArgumentException) { }
    }
}
