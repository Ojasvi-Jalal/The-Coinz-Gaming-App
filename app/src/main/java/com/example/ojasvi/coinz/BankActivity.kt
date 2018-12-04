package com.example.ojasvi.coinz

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth

import kotlinx.android.synthetic.main.activity_bank.*

class BankActivity : Activity() {

    private var balance: TextView? = null
    private var walletButton: ImageView? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank)

        balance = findViewById(R.id.userBalance)
        walletButton = findViewById(R.id.wallet)
        walletButton!!.isEnabled = true

        walletButton!!.setOnClickListener(){
            Log.d(TAG,"Openining wallet")
            intent = Intent(this,WalletActivity::class.java)
            startActivity(intent)
        }
    }

    companion object {
        var TAG = "Bank"
    }

}
