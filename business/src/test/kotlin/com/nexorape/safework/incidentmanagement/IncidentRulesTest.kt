package com.nexorape.safework.incidentmanagement

import com.nexorape.safework.iam.domain.model.*
import com.nexorape.safework.incidentmanagement.application.*
import com.nexorape.safework.incidentmanagement.domain.model.*
import com.nexorape.safework.incidentmanagement.domain.repositories.*
import org.junit.Assert.*
import org.junit.Test

class IncidentRulesTest {
    @Test fun textLimitsAndTransitionsAreDomainRules() {
        IncidentTitle.of("😀".repeat(120)); IncidentDescription.of("d".repeat(4000)); IncidentLocation.of("l".repeat(500))
        invalid { IncidentTitle.of("😀".repeat(121)) }; invalid { IncidentDescription.of("d".repeat(4001)) }
        invalid { IncidentLocation.of(" ") }; invalid { IncidentId(0) }; invalid { IncidentTitle.of("\uD800") }
        assertTrue(IncidentStatus.OPEN.permits(IncidentStatus.ASSIGNED))
        assertTrue(IncidentStatus.ASSIGNED.permits(IncidentStatus.IN_PROGRESS))
        assertTrue(IncidentStatus.IN_PROGRESS.permits(IncidentStatus.CLOSED))
        assertFalse(IncidentStatus.OPEN.permits(IncidentStatus.CLOSED))
        assertFalse(IncidentStatus.CLOSED.permits(IncidentStatus.OPEN))
    }
    private fun invalid(action: () -> Unit) { try { action(); fail("Invalid value accepted") } catch (_: IllegalArgumentException) { } }
}
