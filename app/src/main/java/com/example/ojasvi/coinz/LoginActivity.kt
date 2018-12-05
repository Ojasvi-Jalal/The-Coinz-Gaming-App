package com.example.ojasvi.coinz

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.app.LoaderManager.LoaderCallbacks
import android.content.CursorLoader
import android.content.Loader
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.TextView

import java.util.ArrayList
import android.Manifest.permission.READ_CONTACTS
import android.app.ProgressDialog
import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.toast

/**
 * A login screen that offers login via email/password.
 */
class LoginActivity : AppCompatActivity() {

    private var email_Text: EditText? = null
    private var password_Text: EditText? = null
    private var login_Button: Button? = null
    private var signUpLink: TextView? = null

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email_Text = findViewById<EditText>(R.id.username) as EditText
        password_Text = findViewById<EditText>(R.id.password) as EditText
        login_Button = findViewById<Button>(R.id.sign_in_button) as Button
        signUpLink = findViewById(R.id.sign_up) as TextView

        //Buttons
        login_Button!!.setOnClickListener{login()}

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

        login_Button!!.isEnabled = false

        val progressDialog = ProgressDialog(this@LoginActivity,R.style.AppTheme)

        // Reset errors.
        email_Text?.error = null
        password_Text?.error = null

        // Store values at the time of the login attempt.
        val email = email_Text!!.text.toString()
        val password = password_Text!!.text.toString()

        var cancel = false
        var focusView: View? = null

        Log.d(TAG, "In login activity, got email and password")

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            password_Text?.error = getString(R.string.error_invalid_password)
            focusView = password_Text
            cancel = true
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            email_Text?.error = getString(R.string.error_field_required)
            focusView = email_Text
            cancel = true
        } else if (!isEmailValid(email)) {
            email_Text?.error = getString(R.string.error_invalid_email)
            focusView = email_Text
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            Log.d("Message", "correct email and password were entered")
            login_Button!!.isEnabled = true
            mAuth?.signInWithEmailAndPassword(email, password)
                    ?.addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Login Successful!")
                            toast("Login Successful!")
                            onLoginSuccess()
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

    fun onLoginSuccess(){
        login_Button!!.isEnabled = true
        val intent = Intent(this, MainMenuActivity::class.java)
        startActivity(intent)
    }

    fun onLoginFailed(){
        toast("Login Failed :(")
    }

    companion object {
        private val TAG = "LoginActivity"
        private val REQUEST_SIGNUP = 0
    }

}