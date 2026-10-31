package cu.axel.smartdock.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import cu.axel.smartdock.R
import cu.axel.smartdock.preferences.FileChooserPreference

private const val MENU_REQUEST_CODE = 4

class AppMenuPreferences : PreferenceFragmentCompat() {
    private var menuIconPref: FileChooserPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_app_menu, rootKey)
        menuIconPref = findPreference("menu_icon_uri")
        menuIconPref?.setOnPreferenceClickListener {
            startActivityForResult(
                Intent(Intent.ACTION_OPEN_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("image/*"),
                MENU_REQUEST_CODE
            )
            false
        }
        findPreference<EditTextPreference>("num_columns")?.apply {
            setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER
            }
            setOnPreferenceChangeListener { _, value ->
                val count = value.toString().toIntOrNull()
                count != null && count in 2..10
            }
        }
    }

    @Deprecated("Uses the preference file picker until the Activity Result migration")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != MENU_REQUEST_CODE || resultCode != Activity.RESULT_OK) return
        val uri = data?.data ?: return
        try {
            requireContext().contentResolver.takePersistableUriPermission(
                uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            menuIconPref?.setFile(uri.toString())
        } catch (_: SecurityException) {
            // User may have picked a document provider without persistable grants.
        }
    }
}
