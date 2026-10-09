package com.nexorape.safework.incidentmanagement

import com.nexorape.safework.iam.domain.model.UserId
import com.nexorape.safework.incidentmanagement.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class AssignmentRulesTest {
    @Test fun closedAssignmentRequiresPersistedCompletion() {
        val assignment = Assignment(AssignmentId(1), IncidentId(2), UserId(3), "Synthetic", IncidentStatus.IN_PROGRESS, Instant.EPOCH, AssignmentPriority.MEDIUM, null)
        assertNull(assignment.completionDate)
        try { assignment.copy(status = IncidentStatus.CLOSED); fail("Missing completion accepted") } catch (_: IllegalArgumentException) { }
        assertEquals(Instant.EPOCH, assignment.copy(status = IncidentStatus.CLOSED, completionDate = Instant.EPOCH).completionDate)
    }
    @Test fun responsibilityIdsAndAssignmentStateAreExplicit() {
        try { AssignmentId(0); fail("Nonpositive ID accepted") } catch (_: IllegalArgumentException) { }
        try { Assignment(AssignmentId(1), IncidentId(2), UserId(3), "Synthetic", IncidentStatus.OPEN, Instant.EPOCH, AssignmentPriority.LOW, null); fail("OPEN assignment accepted") }
        catch (_: IllegalArgumentException) { }
    }
}
