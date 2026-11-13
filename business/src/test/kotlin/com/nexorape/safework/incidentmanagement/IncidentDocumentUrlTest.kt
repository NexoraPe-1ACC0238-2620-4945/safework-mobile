package com.nexorape.safework.incidentmanagement

import com.nexorape.safework.incidentmanagement.domain.model.IncidentDocumentUrl
import org.junit.Assert.*
import org.junit.Test

class IncidentDocumentUrlTest {
    @Test fun keepsAnExistingHttpsDocumentReference() {
        assertEquals("https://example.test/manual.pdf?version=2#page=3",
            IncidentDocumentUrl.of(" https://example.test/manual.pdf?version=2#page=3 ").value)
    }

    @Test fun rejectsUnsafeMalformedAndOversizedReferences() {
        listOf("", "http://example.test/file.pdf", "file:///private/file.pdf", "javascript:alert(1)",
            "https://", "https://user:password@example.test/file", "https://example.test:0/file",
            "https://example.test:65536/file", "https://example.test/a b", "https://example.test/a\nb",
            "https://example.test/" + "a".repeat(2048)).forEach { value ->
            try { IncidentDocumentUrl.of(value); fail("Invalid document reference accepted") }
            catch (_: IllegalArgumentException) { }
        }
    }
}
