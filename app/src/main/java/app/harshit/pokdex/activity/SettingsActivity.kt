package app.harshit.pokdex.activity

import android.os.Bundle
import android.preference.PreferenceFragment
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.harshit.pokdex.BuildConfig
import app.harshit.pokdex.R
import com.android.billingclient.api.*
import org.jetbrains.anko.toast

class SettingsActivity : AppCompatPreferenceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentManager.beginTransaction().replace(android.R.id.content, PrefsFragment()).commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class PrefsFragment : PreferenceFragment(), PurchasesUpdatedListener {

        override fun onPurchasesUpdated(responseCode: Int, purchases: MutableList<Purchase>?) {
            if (responseCode == BillingClient.BillingResponse.OK && purchases != null) {
                toast(getString(R.string.purchase_success))
            } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
                toast("¯\\_(ツ)_/¯")
            }
        }

        private lateinit var billingClient: BillingClient

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference)
            billingClient = BillingClient.newBuilder(activity).setListener(this).build()
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

            findPreference("buildVersion").summary = "Version ${BuildConfig.VERSION_NAME}"

            findPreference("buildVersion").setOnPreferenceClickListener {
                AlertDialog.Builder(activity)
                        .setTitle("What's new!")
                        .setMessage("Added frame to the Camera\nAdd tap-to-capture\nAn all new intro screen!")
                        .setPositiveButton("Ok") { _, _ ->

                        }.show()
                true
            }

            findPreference("donate").setOnPreferenceClickListener {
                billingClient.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(@BillingClient.BillingResponse billingResponseCode: Int) {
                        if (billingResponseCode == BillingClient.BillingResponse.OK) {
                            val flowParams = BillingFlowParams.newBuilder()
                                    .setSku(getString(R.string.sku_id))
                                    .setType(BillingClient.SkuType.INAPP) // SkuType.SUB for subscription
                                    .build()
                            billingClient.launchBillingFlow(activity, flowParams)
                        }
                    }

                    override fun onBillingServiceDisconnected() {
                        // Try to restart the connection on the next request to
                        // Google Play by calling the startConnection() method.
                    }
                })
                true
            }

            return super.onCreateView(inflater, container, savedInstanceState)
        }

        companion object {
            private const val LOGIN_PREF = "loginout"
        }

    }

}