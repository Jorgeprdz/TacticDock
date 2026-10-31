package cu.axel.smartdock.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DisplayTargetPolicyTest {
    @Test fun attachedOverlayHasPriority() {
        assertEquals(27, DisplayTargetPolicy.choose(setOf(13, 27), 27, 13, null))
    }
    @Test fun contextUsedBeforeUniqueDisplay() {
        assertEquals(13, DisplayTargetPolicy.choose(setOf(13, 27), null, 13, null))
    }
    @Test fun reconnectionReplacesStaleIdentifier() {
        assertEquals(42, DisplayTargetPolicy.choose(setOf(42), 11, 11, null))
    }
    @Test fun staleExplicitRequestIsRejected() {
        assertNull(DisplayTargetPolicy.choose(setOf(42), 42, 42, 11))
    }
    @Test fun ambiguousWithoutAttachedDockIsRejected() {
        assertNull(DisplayTargetPolicy.choose(setOf(13, 27), null, null, null))
    }
    @Test fun phoneOnlyDoesNotLaunch() {
        assertNull(DisplayTargetPolicy.choose(emptySet(), 0, 0, null))
    }
}

class NotificationDisplayPolicyTest {
    @Test fun popupRequiresDockOnActiveExternalDisplay() {
        org.junit.Assert.assertTrue(NotificationDisplayPolicy.isValidTarget(setOf(25), 25, 25))
    }
    @Test fun phoneMustNeverGetCustomNotificationPopup() {
        org.junit.Assert.assertFalse(NotificationDisplayPolicy.isValidTarget(setOf(25), 0, 0))
    }
    @Test fun disconnectedDisplayCannotShowPopups() {
        org.junit.Assert.assertFalse(NotificationDisplayPolicy.isValidTarget(emptySet(), 25, 25))
    }
    @Test fun mismatchedOverlayContextIsBlocked() {
        org.junit.Assert.assertFalse(NotificationDisplayPolicy.isValidTarget(setOf(25), 25, 0))
    }
    @Test fun monitorReconnectionReevaluatesTarget() {
        org.junit.Assert.assertFalse(NotificationDisplayPolicy.isValidTarget(setOf(57), 25, 25))
        org.junit.Assert.assertTrue(NotificationDisplayPolicy.isValidTarget(setOf(57), 57, 57))
    }
    @Test fun ambiguousMultipleDisplaysUseAttachedDockOnly() {
        org.junit.Assert.assertTrue(NotificationDisplayPolicy.isValidTarget(setOf(5, 57), 57, 57))
        org.junit.Assert.assertFalse(NotificationDisplayPolicy.isValidTarget(setOf(5, 57), null, 57))
    }
}
