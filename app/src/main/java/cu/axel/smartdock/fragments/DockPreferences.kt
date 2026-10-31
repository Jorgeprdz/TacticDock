package cu.axel.smartdock.fragments

import android.os.Bundle
import android.text.InputType
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import cu.axel.smartdock.R

/** DeX's Dock has a single fixed layout. Only actual runtime settings are exposed. */
class DockPreferences : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_dock, rootKey)
        findPreference<EditTextPreference>("max_running_apps_landscape")
            ?.apply {
                setOnBindEditTextListener { editor ->
                    editor.inputType = InputType.TYPE_CLASS_NUMBER
                }
                setOnPreferenceChangeListener { _, value ->
                    val count = value.toString().toIntOrNull()
                    count != null && count in 1..40
                }
            }
    }
}
