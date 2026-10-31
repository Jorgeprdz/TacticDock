package cu.axel.smartdock.utils

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.os.Process
import android.os.Bundle
import android.util.Log

/**
 * Performs operations related to deep shortcuts, such as querying for them, pinning them, etc.
 */
object DeepShortcutManager {
    fun startShortcut(shortcutInfo: ShortcutInfo, context: Context, options: Bundle): Boolean {
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        return try {
            launcherApps.startShortcut(
                shortcutInfo.getPackage(), shortcutInfo.id, null, options, shortcutInfo.userHandle
            )
            true
        } catch (error: Exception) {
            Log.e("TacticDock", "Shortcut launch failed: ${shortcutInfo.id}", error)
            false
        }
    }

    fun getShortcutIcon(shortcutInfo: ShortcutInfo, context: Context): Drawable? {
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        try {
            val density = context.resources.displayMetrics.density * 48
            return launcherApps.getShortcutIconDrawable(shortcutInfo, density.toInt())
        } catch (_: Exception) {
        }
        return null
    }

    fun getShortcuts(app: String, context: Context): List<ShortcutInfo>? {
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        val queryParams = LauncherApps.ShortcutQuery()
        queryParams.setQueryFlags(
                LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC)
        queryParams.setPackage(app)
        return try {
            launcherApps.getShortcuts(queryParams, Process.myUserHandle())
        } catch (_: Exception) {
            null
        }
    }

    fun hasHostPermission(context: Context): Boolean {
        val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        return launcherApps.hasShortcutHostPermission()
    }
}
