package app.harshit.pokdex

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager

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
