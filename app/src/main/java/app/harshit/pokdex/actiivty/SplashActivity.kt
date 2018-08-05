package app.harshit.pokdex.actiivty

import android.app.AlertDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth

fun isUserSignedIn() = FirebaseAuth.getInstance().currentUser != null

fun startAuth(activity: AppCompatActivity) {
    activity.startActivity(Intent(activity, IntroActivity::class.java))
    activity.finish()
}

fun logoutAndStartAuth(activity: AppCompatActivity) {
    val alertDialog = AlertDialog.Builder(activity)
            .setTitle("Do you wish to log out?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.cancel()
                AuthUI.getInstance().signOut(activity)
                activity.startActivity(Intent(activity, IntroActivity::class.java))
                activity.finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                //Do nothing
                dialog.cancel()
            }.create()
    alertDialog.show()
}

fun getCurrentUser() = FirebaseAuth.getInstance().currentUser

const val RC_SIGN_IN = 4204

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("INTRO", true))
            startActivity(Intent(this, IntroActivity::class.java))
        else
            startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
