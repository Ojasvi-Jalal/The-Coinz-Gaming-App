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


    private var email_Text: EditText? = null
    private var password_Text: EditText? = null
    private var username_Text: EditText? = null
    private var login_Button: Button? = null
    private var sign_Up_Button: TextView? = null

    private var mAuth: FirebaseAuth? = null
    private var userInfo: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        mAuth = FirebaseAuth.getInstance()
        userInfo = FirebaseFirestore.getInstance()
        email_Text  = findViewById<EditText>(R.id.emailSignUp)
        password_Text = findViewById<EditText>(R.id.passwordSignUp)
        username_Text = findViewById(R.id.sendee)
        login_Button = findViewById<Button>(R.id.login) as Button
        sign_Up_Button = findViewById(R.id.register)

        sign_Up_Button!!.setOnClickListener(){signup()}

        login_Button!!.setOnClickListener(){
            //Finish the registration screen and return to the Login activity
            val intent = Intent(applicationContext, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    fun signup(){
        Log.d(TAG, "User Registration")

        login_Button!!.isEnabled = true

        // Reset errors.
        email_Text?.error = null
        password_Text?.error = null

        // Store values at the time of the login attempt.
        val email = email_Text!!.text.toString()
        val password = password_Text!!.text.toString()
        val nickname = username_Text!!.text.toString()

        var cancel = false
        var focusView: View? = null

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

            mAuth?.createUserWithEmailAndPassword(email, password)
                    ?.addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "Registration Successful!")
                            toast("Registration Successful!")
                        } else {
                            //Sign in failed, display a message to the User
                            Log.d(TAG, "Registration Failed :( ${task.exception}")
                            toast("Registration Failed :(, Please Try Again!")
                        }

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

    companion object {
        private val TAG = "SignUpActivity"
        private val REQUEST_SIGNUP = 0
    }
}
