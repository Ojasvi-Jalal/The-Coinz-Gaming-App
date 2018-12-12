package com.example.ojasvi.coinz

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.toast

class SignUpActivity : AppCompatActivity() {

    //user's email
    private var emailText: EditText? = null
    //user's password
    private var passwordText: EditText? = null
    //username(nickname)
    private var usernameText: EditText? = null
    //login button to lead to the main menu
    private var loginButton: Button? = null
    //does registration
    private var signUpButton: TextView? = null

    //initialise firebase auth
    private var mAuth: FirebaseAuth? = null
    //initialise firestore
    private var userInfo: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //sets the layout to be activity_sign_up
        setContentView(R.layout.activity_sign_up)

        //set the firebase auth
        mAuth = FirebaseAuth.getInstance()
        //set the firestore
        userInfo = FirebaseFirestore.getInstance()

        //set the fields to the relevant views in the layout
        emailText  = findViewById(R.id.emailSignUp)
        passwordText = findViewById(R.id.passwordSignUp)
        usernameText = findViewById(R.id.sendee)
        loginButton = findViewById(R.id.login)
        signUpButton = findViewById(R.id.register)

        //user clicks on signUp
        signUpButton!!.setOnClickListener{signup()}

        loginButton!!.setOnClickListener{
            //Finish the registration screen and return to the Login activity
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun signup(){
        Log.d(TAG, "User Registration")

        loginButton!!.isEnabled = true

        // Reset errors.
        emailText?.error = null
        passwordText?.error = null

        // Store values at the time of the login attempt.
        val email = emailText!!.text.toString()
        val password = passwordText!!.text.toString()
        val nickname = usernameText!!.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            passwordText?.error = getString(R.string.error_invalid_password)
            focusView = passwordText
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            emailText?.error = getString(R.string.error_field_required)
            focusView = emailText
            cancel = true
        } else if (!isEmailValid(email)) {
            emailText?.error = getString(R.string.error_invalid_email)
            focusView = emailText
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        }
        else
        {
            Log.d("Message", "correct email and password were entered")

            mAuth?.createUserWithEmailAndPassword(email, password)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful)
                {
                    //create User With Email And Password on Firebase
                    Log.d(TAG, "Registration Successful!")
                    toast("Registration Successful!")
                    val ref = userInfo?.collection("wallets")
                            ?.document(mAuth?.currentUser?.email!!)

                    val username = HashMap<String, Any?>()
                    username["Nickname"]= nickname

                    ref?.collection("User info")
                            ?.document("Nickname")
                            ?.set(username)
                            ?.addOnSuccessListener { Log.d(TAG, "Nickname successfully written!") }
                            ?.addOnFailureListener { e -> Log.w(TAG, "Error writing document", e)}
                }
                else
                {
                    //Sign in failed, display a message to the User
                    Log.d(TAG, "Registration Failed :( ${task.exception}")
                    toast("Registration Failed :(, Please Try Again!")
                }
            }
        }



    }

    private fun isPasswordValid(password: String): Boolean {
        //not very strict: to make it more user friendly;
        //Suggestion: Perhaps could become more restrictive in the future
        return password.length > 6
    }

    private fun isEmailValid(email: String): Boolean {
        //not very strict: to make it more user friendly;
        //Suggestion: Perhaps could become more restrictive in the future
        return email.contains("@")
    }

    companion object {
        private const val TAG = "SignUpActivity"
    }
}
