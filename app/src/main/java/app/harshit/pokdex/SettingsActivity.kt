package app.harshit.pokdex

import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.MenuItem

class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManager.beginTransaction().replace(android.R.id.content, PrefsFragment()).commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class PrefsFragment : PreferenceFragment() {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
