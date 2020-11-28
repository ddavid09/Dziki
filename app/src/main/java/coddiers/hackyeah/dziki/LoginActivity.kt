package coddiers.hackyeah.dziki

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import coddiers.hackyeah.dziki.database.DataBase
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging


class LoginActivity : AppCompatActivity() {
    lateinit var mainMenuIntent: Intent
    val TAG = "LoginActivty"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        title = "Zaloguj"
        mainMenuIntent = Intent(this, MainActivity::class.java)
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().getCurrentUser()
        if(currentUser!=null){
            startActivity(this.mainMenuIntent)
        }
        else{
            createSignInIntent()
        }


    }
    private fun createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.ic_logo)
                .build(),
            RC_SIGN_IN)
        // [END auth_fui_create_intent]

        this.actionBar?.title = "Zaloguj"
    }

    // [START auth_fui_result]
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.d(TAG,"requestCode:"+requestCode)
        Log.d(TAG,"resultCode:"+resultCode)


        if (requestCode == RC_SIGN_IN) {


            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG,"Login succces:"+resultCode)


            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result

                // Log and toast
                Log.d(TAG, "token: "+token)
                DataBase().uploadToken(token)
            })
            startActivity(this.mainMenuIntent)
        }
    }
    // [END auth_fui_result]

    private fun signOut() {
        // [START auth_fui_signout]
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                // ...
            }
        // [END auth_fui_signout]
    }

    //    private fun delete() {
//        // [START auth_fui_delete]
//        AuthUI.getInstance()
//            .delete(this)
//            .addOnCompleteListener {
//                // ...
//            }
//        // [END auth_fui_delete]
//    }
//
//    private fun themeAndLogo() {
//        val providers = emptyList<AuthUI.IdpConfig>()
//
//        // [START auth_fui_theme_logo]
//        startActivityForResult(
//            AuthUI.getInstance()
//                .createSignInIntentBuilder()
//                .setAvailableProviders(providers)
//                .setLogo(R.drawable.logo) // Set logo drawable
//                .build(),
//            RC_SIGN_IN)
//        // [END auth_fui_theme_logo]
//    }
//
//    private fun privacyAndTerms() {
//        val providers = emptyList<AuthUI.IdpConfig>()
//        // [START auth_fui_pp_tos]
//        startActivityForResult(
//            AuthUI.getInstance()
//                .createSignInIntentBuilder()
//                .setAvailableProviders(providers)
//                .setTosAndPrivacyPolicyUrls(
//                    "https://example.com/terms.html",
//                    "https://example.com/privacy.html")
//                .build(),
//            RC_SIGN_IN)
//        // [END auth_fui_pp_tos]
//    }
//
    companion object {

        private const val RC_SIGN_IN = 123
    }
}