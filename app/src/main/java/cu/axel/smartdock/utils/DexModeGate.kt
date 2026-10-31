package cu.axel.smartdock.utils

import android.content.Context
import android.content.res.Configuration
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.Log
import android.view.Display

/** Pure fail-closed policy; display IDs are runtime values, never constants. */
internal object DexModePolicy {
    fun mayShowDock(samsung: Boolean, externalDisplays: Int, dexConfirmed: Boolean): Boolean =
        samsung && externalDisplays == 1 && dexConfirmed
}

/**
 * Gate ALL Tactic Dock windows behind an active Samsung DeX dual-display session.
 *
 * A connected HDMI display may be mirroring the phone; that is NOT DeX.
 * Samsung's official desktopmode API is preferred; DeX Configuration fields
 * are a compatibility fallback when that API is inaccessible.
 */
internal object DexModeGate {
    fun isDexExternalActive(context: Context, manager: DisplayManager): Boolean =
        findActiveExternalDisplay(context, manager) != null

    fun findActiveExternalDisplay(context: Context, manager: DisplayManager): Display? {
        val external = manager.displays.filter {
            it.isValid && it.displayId != Display.DEFAULT_DISPLAY
        }
        if (!Build.MANUFACTURER.equals("samsung", ignoreCase = true) || external.size != 1) {
            return null
        }
        val display = external.single()
        val enabled = dexManagerDualMode(context)
            ?: isDexConfigurationEnabled(context.createDisplayContext(display).resources.configuration)
        val eligible = DexModePolicy.mayShowDock(true, external.size, enabled)
        Log.d("TacticDock", "DeX gate external=${display.displayId} dexConfirmed=$enabled allowed=$eligible")
        return display.takeIf { eligible }
    }

    private fun dexManagerDualMode(context: Context): Boolean? = try {
        val desktop = context.applicationContext.getSystemService("desktopmode")
            ?: return null
        val state = desktop.javaClass.getMethod("getDesktopModeState").invoke(desktop)
            ?: return null
        val cls = state.javaClass
        val enabled = cls.getMethod("getEnabled").invoke(state) as Int
        val type = cls.getMethod("getDisplayType").invoke(state) as Int
        val enabledCode = cls.getDeclaredField("ENABLED").getInt(null)
        val dualCode = cls.getDeclaredField("DISPLAY_TYPE_DUAL").getInt(null)
        enabled == enabledCode && type == dualCode
    } catch (error: ReflectiveOperationException) {
        Log.d("TacticDock", "DeX manager reflection unavailable: ${error.javaClass.simpleName}")
        null
    } catch (error: SecurityException) {
        Log.d("TacticDock", "DeX manager restricted")
        null
    }

    private fun isDexConfigurationEnabled(config: Configuration): Boolean = try {
        val cls = config.javaClass
        val enabledCode = cls.getField("SEM_DESKTOP_MODE_ENABLED").getInt(null)
        cls.getField("semDesktopModeEnabled").getInt(config) == enabledCode
    } catch (_: ReflectiveOperationException) {
        false
    } catch (_: SecurityException) {
        false
    }
}
