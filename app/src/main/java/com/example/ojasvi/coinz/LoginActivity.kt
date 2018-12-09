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

    private var emailText: EditText? = null
    private var passwordText: EditText? = null
    private var loginButton: Button? = null
    private var signUpLink: TextView? = null

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        emailText = findViewById<EditText>(R.id.username)
        passwordText = findViewById<EditText>(R.id.password) as EditText
        loginButton = findViewById<Button>(R.id.sign_in_button) as Button
        signUpLink = findViewById(R.id.sign_up)

        //Buttons

        loginButton!!.setOnClickListener { login() }

        mAuth = FirebaseAuth.getInstance()

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

    fun login(){
        Log.d("Message","Login")

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
        } else {
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
        //TODO: Replace this with your own logic
        return password.length > 4
    }

    private fun isEmailValid(email: String): Boolean {
        //TODO: Replace this with your own logic
        return email.contains("@")
    }

    private fun onLoginSuccess() {
        loginButton!!.isEnabled = true
        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
    }

    companion object {
        private const val TAG = "LoginActivity"
        private const val REQUEST_SIGNUP = 0
    }

}