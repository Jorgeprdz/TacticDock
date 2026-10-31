package cu.axel.smartdock.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import cu.axel.smartdock.R

/** Tactic Dock project details and open-source attribution. */
class HelpAboutPreferences : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences_help_about, rootKey)
    }
}
