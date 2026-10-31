package cu.axel.smartdock.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import cu.axel.smartdock.R
import cu.axel.smartdock.utils.DeviceUtils
import cu.axel.smartdock.utils.Utils

private const val SAVE_REQUEST_CODE = 236
private const val OPEN_REQUEST_CODE = 632

/** Non-privileged diagnostics; never mutate SystemUI, secure settings or root state. */
class AdvancedPreferences : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_advanced, rootKey)
        findPreference<Preference>("share_display_info")?.setOnPreferenceClickListener {
            val info = DeviceUtils.getDisplays(requireContext()).joinToString("\n") { display ->
                "ID=${display.displayId} valid=${display.isValid} name=${display.name}"
            }
            startActivity(
                Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, info)
                    },
                    getString(R.string.tactic_display_info)
                )
            )
            true
        }
        findPreference<Preference>("backup_preferences")?.setOnPreferenceClickListener {
            startActivityForResult(
                Intent(Intent.ACTION_CREATE_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("application/octet-stream")
                    .putExtra(
                        Intent.EXTRA_TITLE,
                        "TacticDock_config_${Utils.currentDateString}.sdp"
                    ),
                SAVE_REQUEST_CODE
            )
            true
        }
        findPreference<Preference>("restore_preferences")?.setOnPreferenceClickListener {
            startActivityForResult(
                Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("*/*"),
                OPEN_REQUEST_CODE
            )
            true
        }
    }

    @Deprecated("Uses the system document picker until the Activity Result migration")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        val uri = data?.data ?: return
        if (requestCode == SAVE_REQUEST_CODE) Utils.backupPreferences(requireContext(), uri)
        if (requestCode == OPEN_REQUEST_CODE) Utils.restorePreferences(requireContext(), uri)
    }
}
