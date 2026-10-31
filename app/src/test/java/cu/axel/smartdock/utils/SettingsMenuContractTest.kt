package cu.axel.smartdock.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/** Guards against reintroducing legacy phone-oriented SmartDock settings. */
class SettingsMenuContractTest {
    private fun resource(name: String): File =
        File(System.getProperty("user.dir"), "src/main/res/xml/$name.xml")

    @Test fun mainNavigationHasExactlySixDeXRelevantSections() {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(resource("preferences_main"))
        val headers = doc.getElementsByTagName("cu.axel.smartdock.preferences.PreferenceHeader")
        assertEquals(6, headers.length)
        val source = resource("preferences_main").readText()
        listOf("DockPreferences", "AppearancePreferences", "AppMenuPreferences",
            "NotificationPreferences", "AdvancedPreferences", "HelpAboutPreferences")
            .forEach { assertTrue(source.contains(it)) }
        listOf("DesktopPreferences", "DefaultAppsPreferences", "KeyboardPreferences",
            "HotCornersPreferences", "SoundsPreferences")
            .forEach { assertFalse(source.contains(it)) }
    }

    @Test fun dangerousLegacySettingsAreNotExposed() {
        val resources = listOf("preferences_main", "preferences_dock",
            "preferences_advanced", "preferences_appearance",
            "preferences_app_menu", "preferences_notification")
        val source = resources.joinToString("\n") { resource(it).readText() }
        listOf("disable_heads_up", "soft_reboot", "allow_sysapp_uninstall",
            "allow_app_freeze", "prefer_last_display", "custom_display_size",
            "status_icon_blacklist", "dock_activation_area", "activation_method",
            "hot_corners_delay", "app_menu_fullscreen", "user_name")
            .forEach { assertFalse("Unexpected legacy setting $it", source.contains("android:key=\"$it\"")) }
    }

    @Test fun essentialsRemainDiscoverable() {
        val source = listOf("preferences_dock", "preferences_appearance",
            "preferences_app_menu", "preferences_notification", "preferences_advanced")
            .joinToString("\n") { resource(it).readText() }
        listOf("max_running_apps_landscape", "icon_pack", "theme",
            "menu_icon_uri", "launch_mode", "show_notifications",
            "enable_notif_panel", "backup_preferences", "restore_preferences",
            "share_display_info")
            .forEach { assertTrue("Missing essential $it", source.contains("android:key=\"$it\"")) }
    }
}
