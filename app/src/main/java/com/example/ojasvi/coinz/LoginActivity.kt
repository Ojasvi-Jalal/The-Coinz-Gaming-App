@file:Suppress("DEPRECATION")

package com.example.ojasvi.coinz

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import org.jetbrains.anko.toast

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {

    //user's email
    private var emailText: EditText? = null
    //user's password
    private var passwordText: EditText? = null
    //login button to lead to the main menu
    private var loginButton: Button? = null
    //link to make a user account
    private var signUpLink: TextView? = null

    //initialise firebase auth
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        //sets the layout to be activity_login
        setContentView(R.layout.activity_login)

        //set the fields and the buttons according to the activity login
        emailText = findViewById(R.id.username)
        passwordText = findViewById(R.id.password)
        loginButton = findViewById(R.id.sign_in_button)
        signUpLink = findViewById(R.id.sign_up)

        //if user clicks on login
        loginButton!!.setOnClickListener { login() }

        //set the firebase auth
        mAuth = FirebaseAuth.getInstance()

        //if a user is already logged in then directly opens the main menu
        if(mAuth?.currentUser != null)
            onLoginSuccess()

        signUpLink!!.setOnClickListener {
            //Start the register activity
            Log.d(TAG,"Starting Registration")
            val intent = Intent(this, SignUpActivity::class.java)
            startActivityForResult(intent, REQUEST_SIGNUP)
            finish()
        }
    }

    private fun login(){
        Log.d("Message","Login")

        //login button disabled until further notice
        loginButton!!.isEnabled = false

        @Suppress("DEPRECATION")
        ProgressDialog(this@LoginActivity, R.style.AppTheme)

        // Reset errors.
        emailText?.error = null
        passwordText?.error = null

        // Store values at the time of the login attempt.
        val email = emailText!!.text.toString()
        val password = passwordText!!.text.toString()


        var cancel = false
        var focusView: View? = null

        Log.d(TAG, "In login activity, got email and password")

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password))
        {
            passwordText?.error = getString(R.string.error_invalid_password)
            focusView = passwordText
            cancel = true
        }

        // Check for a valid email address.
        //  If the email field is empty
        if (TextUtils.isEmpty(email))
        {
            emailText?.error = getString(R.string.error_field_required)
            focusView = emailText
            cancel = true
        }
        // If the email doesn't meet the criterion
        else if (!isEmailValid(email))
        {
            emailText?.error = getString(R.string.error_invalid_email)
            focusView = emailText
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // The user entered valid credentials
            Log.d("Message", "correct email and password were entered")
            loginButton!!.isEnabled = true
            mAuth?.signInWithEmailAndPassword(email, password)
                    ?.addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Login Successful!")
                            toast("Login Successful!")
                            this.onLoginSuccess()
                        } else {
                            //Sign in failed, display a message to the User
                            Log.d(TAG, "Login Failed :( ${task.exception}")
                            toast("Login Failed :(")
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

    private fun onLoginSuccess() {
        //valid credentials entered: enable the login button
        loginButton!!.isEnabled = true
        //switch to the main menu activityz
        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "LoginActivity"
        private const val REQUEST_SIGNUP = 0
    }

}