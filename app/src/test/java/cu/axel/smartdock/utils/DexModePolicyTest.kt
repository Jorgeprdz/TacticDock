package cu.axel.smartdock.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DexModePolicyTest {
    @Test fun phoneOnlyCannotDrawDock() {
        assertFalse(DexModePolicy.mayShowDock(true, 0, false))
        assertFalse(DexModePolicy.mayShowDock(true, 0, true))
    }
    @Test fun hdmiMirrorCannotDrawDock() {
        assertFalse(DexModePolicy.mayShowDock(true, 1, false))
    }
    @Test fun verifiedDexAndOneExternalDisplayDrawsDock() {
        assertTrue(DexModePolicy.mayShowDock(true, 1, true))
    }
    @Test fun noSamsungDexOnOtherManufacturers() {
        assertFalse(DexModePolicy.mayShowDock(false, 1, true))
    }
    @Test fun multipleDisplaysFailClosedWhenTargetAmbiguous() {
        assertFalse(DexModePolicy.mayShowDock(true, 2, true))
    }
    @Test fun disconnectedDexImmediatelyFailsClosed() {
        assertTrue(DexModePolicy.mayShowDock(true, 1, true))
        assertFalse(DexModePolicy.mayShowDock(true, 0, false))
    }
}
