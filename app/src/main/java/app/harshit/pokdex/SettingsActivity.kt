package app.harshit.pokdex

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth

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

            if (FirebaseAuth.getInstance().currentUser == null)
                findPreference("loginout").summary = "Click to log in"
            else {
                findPreference("loginout").title = "${FirebaseAuth.getInstance().currentUser?.displayName}"
                findPreference("loginout").summary = "Click to log out"
            }

            findPreference("loginout").setOnPreferenceClickListener {
                if (FirebaseAuth.getInstance().currentUser != null) {
                    //If the user was logged in, log him/her out
                    val alertDialog = AlertDialog.Builder(activity)
                            .setTitle("Do you wish to log out?")
                            .setPositiveButton("Yes") { dialog, _ ->
                                dialog.cancel()
                                AuthUI.getInstance().signOut(activity)
                                startActivity(Intent(activity, IntroActivity::class.java))
                                activity.finish()
                            }
                            .setNegativeButton("No") { dialog, _ ->
                                //Do nothing
                                dialog.cancel()
                            }.create()
                    alertDialog.show()
                } else {
                    //Else log him/her in
                    startActivity(Intent(activity, IntroActivity::class.java))
                    activity.finish()
                }
                true
            }

            return super.onCreateView(inflater, container, savedInstanceState)
        }

    }

}
