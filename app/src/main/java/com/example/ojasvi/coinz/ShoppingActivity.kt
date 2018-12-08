package com.example.ojasvi.coinz

import android.os.Bundle
import android.app.Activity
import android.util.Log
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import kotlinx.android.synthetic.main.activity_shopping.*
import org.jetbrains.anko.toast

class ShoppingActivity : Activity() {

    private var buyCarButton: ImageView? = null
    private var buyBungalowButton: ImageView? = null
    private var buyGoldButton: ImageView? = null
    private var buyGoldXlButton: ImageView? = null

    //Firebase
    //wallet
    private var availableFunds: FirebaseFirestore? = null
    private var mAuth: FirebaseAuth? = null

    private var balance: Long? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping)

        availableFunds = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        buyGoldButton = findViewById(R.id.buy1)
        buyGoldXlButton = findViewById(R.id.buy2)
        buyCarButton = findViewById(R.id.buy3)
        buyBungalowButton = findViewById(R.id.buy4)

        buyBungalowButton?.isEnabled = false
        buyGoldXlButton?.isEnabled = false
        buyCarButton?.isEnabled = false
        buyBungalowButton?.isEnabled = false

        val ref = availableFunds?.collection("wallets")
                ?.document(mAuth?.currentUser?.email!!)

        ref?.collection("User info")
                        ?.document("Available funds")
                        ?.get()
                        ?.addOnSuccessListener {
                    if(it.exists() && it != null) {
                    balance = it.getDouble("Account Balance")?.toLong()
                    buyBungalowButton?.isEnabled = true
                    buyGoldXlButton?.isEnabled = true
                    buyCarButton?.isEnabled = true
                    buyBungalowButton?.isEnabled = true
                }
            }

            buyGoldButton?.setOnClickListener{
                Log.d(TAG,"User wants to buy gold")
                if(balance!! >100){
                    balance = balance!! + 50
                    toast("Congratulations you bought gold worth 150!")
                }
            }

            buyGoldXlButton?.setOnClickListener {
                Log.d(TAG,"User wants to buy a lot of gold")
                if(balance!! >500){
                    balance = balance!!+ 500
                    toast("Congratulations you bought gold worth 1000!")
                }
            }

            buyCarButton?.setOnClickListener {
                Log.d(TAG,"User wants to buy a car")
                if(balance!! >1000){
                    balance = balance!!- 1000
                    toast("Congratulations on your new car!")
                }
            }

            buyBungalowButton?.setOnClickListener {
                Log.d(TAG,"User wants to buy a bungalow")
                if(balance!! >5000){
                    balance = balance!!- 5000
                    toast("Congratulations on your new bungalow!")
                }
            }
        }

    override fun onStop(){
        super.onStop()
        val netWorth = HashMap<String, Any?>()
        netWorth["Account Balance"]= balance
        val ref = availableFunds?.collection("wallets")
                ?.document(mAuth?.currentUser?.email!!)

        ref?.collection("User info")
            ?.document("Available funds")
            ?.set(netWorth)
            ?.addOnSuccessListener { Log.d(TAG, "Account Balance successfully written!") }
            ?.addOnFailureListener { e -> Log.w(TAG, "Error writing document", e)}
    }

    companion object {
        var TAG = "Shopping"
    }



}
