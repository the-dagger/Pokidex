package app.harshit.pokdex.activity

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import app.harshit.pokdex.R
import com.github.paolorotolo.appintro.AppIntro2
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton.*
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.fragment_intro_login.*

class IntroActivity : AppIntro2() {

    lateinit var introLoginFragment: IntroLoginFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showSkipButton(false)
        introLoginFragment = IntroLoginFragment.newInstance(getString(R.string.intro_1_title),
                getString(R.string.login_desc),
                R.drawable.pokeball,
                ContextCompat.getColor(this, R.color.colorPrimary),
                true)
        addSlide(IntroLoginFragment.newInstance(getString(R.string.intro_1_title),
                getString(R.string.intro_1_desc),
                R.drawable.pokedex,
                ContextCompat.getColor(this, R.color.colorPrimary),
                false))
        addSlide(introLoginFragment)
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("INTRO", false).apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        introLoginFragment.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.w("TAG", "Google sign in failed", e)
            }
        }

    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        introLoginFragment.disableGoogleBtn()
                        Toast.makeText(this, "Welcome ${FirebaseAuth.getInstance().currentUser?.displayName}!", Toast.LENGTH_SHORT).show()
                    } else {
                        // If sign in fails, display a message to the user.
                    }
                }
    }

    private fun startLogin(activity: AppCompatActivity) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("948655078679-9a3pn01sjfoi7lcfbbgq28q85t6t786i.apps.googleusercontent.com")
                .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(activity, gso)
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    class IntroLoginFragment : Fragment() {

        companion object {
            fun newInstance(title: String, description: String, @DrawableRes image: Int, @ColorInt color: Int, btn: Boolean): IntroLoginFragment {
                val bundle = Bundle()
                with(bundle) {
                    putInt("IMAGE", image)
                    putInt("COLOR", color)
                    putString("TITLE", title)
                    putString("DESC", description)
                    putBoolean("BUTTON", btn)
                }
                val introLoginFragment = IntroLoginFragment()
                introLoginFragment.arguments = bundle

                return introLoginFragment
            }
        }

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
            return inflater.inflate(R.layout.fragment_intro_login, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

            val bundle = arguments!!

            with(bundle) {
                googleButton.visibility = if (getBoolean("BUTTON")) View.VISIBLE else View.GONE
                googleButton.setOnClickListener {
                    (activity as IntroActivity).startLogin(activity as AppCompatActivity)
                }
                googleButton.setStyle(SIZE_WIDE, COLOR_LIGHT)
                description.text = getString("DESC")
                title.text = getString("TITLE")
                image.setImageDrawable(ContextCompat.getDrawable(context!!, getInt("IMAGE")))
                parent.setBackgroundColor(getInt("COLOR"))
            }
        }

        fun disableGoogleBtn() {
            googleButton.isEnabled = false
        }


    }

}