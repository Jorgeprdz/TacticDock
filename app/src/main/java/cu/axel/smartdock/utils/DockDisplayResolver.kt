package cu.axel.smartdock.utils

import android.content.Context
import android.hardware.display.DisplayManager
import android.util.Log
import android.view.Display
import android.view.View

internal object DisplayTargetPolicy {
    fun choose(
        externalIds: Set<Int>, renderedId: Int?, contextId: Int?, requestedId: Int?
    ): Int? {
        if (requestedId != null) return requestedId.takeIf { it in externalIds }
        return renderedId?.takeIf { it in externalIds }
            ?: contextId?.takeIf { it in externalIds }
            ?: externalIds.singleOrNull()
    }
}

/** Resolve at the time of each click: never cache logical display IDs. */
class DockDisplayResolver(private val manager: DisplayManager) {
    fun isLiveExternal(id: Int): Boolean =
        id != Display.DEFAULT_DISPLAY && manager.getDisplay(id)?.isValid == true

    fun resolve(dock: View?, context: Context, requestedId: Int? = null): Int? {
        val available = manager.displays.filter {
            it.isValid && it.displayId != Display.DEFAULT_DISPLAY
        }.map { it.displayId }.toSet()
        val actual = dock?.takeIf { it.isAttachedToWindow }?.display?.displayId
        val contextual = try { context.display?.displayId }
            catch (_: UnsupportedOperationException) { null }
        val result = DisplayTargetPolicy.choose(available, actual, contextual, requestedId)
        Log.d("TacticDock", "external=$available dock=$actual context=$contextual request=$requestedId result=$result")
        return result
    }
}
