package com.alex_kind.createaccount

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.alex_kind.createaccount.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    lateinit var laucher: ActivityResultLauncher<Intent>
    lateinit var auth: FirebaseAuth

    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        auth.currentUser

        binding.tvRegistration.setOnClickListener {
            startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            onBackPressed()
        }

        loginEmail()
        loginWithGoogle()
    }




    private fun loginEmail() {

        binding.buttonLogin.setOnClickListener {
            when {
                TextUtils.isEmpty(binding.editEmailLogin.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this@LoginActivity,
                        "Please enter email.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                TextUtils.isEmpty(binding.editPasswordLogin.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this@LoginActivity,
                        "Please enter password.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
                else -> {
                    val email = binding.editEmailLogin.text.toString().trim { it <= ' ' }
                    val password = binding.editPasswordLogin.text.toString().trim { it <= ' ' }

                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "You ara logged in successfully.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra(
                                    "userid",
                                    FirebaseAuth.getInstance().currentUser?.uid
                                ) // in other activity call intent.getStringExtra("userid")
                                intent.putExtra("email_id", email)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(
                                    this@LoginActivity,
                                    task.exception!!.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                }
            }
        }
    }

    private fun loginWithGoogle() {

        laucher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(it.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    firebaseAuthWithGoogle(account.idToken)
                }
            } catch (e: ApiException) {
                Log.d("FirebaseGoogleLog", "Api exception")
            }

        }


        binding.buttonLoginWithGoogle.setOnClickListener {
            signIn()
        }

        checkAuthState()


    }

    private fun getClient(): GoogleSignInClient {
        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) //don't have an error
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(this, gso)
    }

    private fun signIn() {

        val signIClient = getClient()
        laucher.launch(signIClient.signInIntent)
    }


    private fun firebaseAuthWithGoogle(idToken: String) {

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Log.d("FirebaseGoogleLog", "Google signIn done")
                    checkAuthState()

                } else {
                    Log.d("FirebaseGoogleLog", "Google signIn incorrect")
                }
            }
    }

    private fun checkAuthState() {
        if (auth.currentUser != null) {
            val intent = Intent(this@LoginActivity, MainActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(
                "userid",
                FirebaseAuth.getInstance().currentUser?.uid
            ) // in other activity call intent.getStringExtra("userid")
            startActivity(intent)
            finish()
        }

    }
    //Google Auth end.


}