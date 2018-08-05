package app.harshit.pokdex.actiivty

import android.os.Bundle
import android.preference.PreferenceFragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.harshit.pokdex.*

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

        override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {

            if (isUserSignedIn()) {
                findPreference(LOGIN_PREF).title = "${getCurrentUser()?.displayName}"
                findPreference(LOGIN_PREF).summary = "Click to log out"
            } else {
                findPreference(LOGIN_PREF).title = "Login"
                findPreference(LOGIN_PREF).summary = "Click to log in"
            }

            findPreference(LOGIN_PREF).setOnPreferenceClickListener {
                if (isUserSignedIn()) {
                    //If the user was logged in, log him/her out
                    logoutAndStartAuth(activity)
                } else {
                    //Else log him/her in
                    startAuth(activity)
                }
                true
            }

            return super.onCreateView(inflater, container, savedInstanceState)
        }

        companion object {
            private const val LOGIN_PREF = "loginout"
        }

    }

}