package com.example.ojasvi.coinz

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.toast

class ShoppingActivity : Activity() {

    //buy the car
    private var buyCarButton: ImageView? = null
    //buy the bungalow
    private var buyBungalowButton: ImageView? = null
    //buy gold
    private var buyGoldButton: ImageView? = null
    //buy extra gold
    private var buyGoldXlButton: ImageView? = null

    //FirebaseAuth and Firestore objects
    private var availableFunds: FirebaseFirestore? = null
    private var mAuth: FirebaseAuth? = null

    //keeps track of the user's balance
    private var balance: Long? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping)

        //get the firestore and firebase instances
        availableFunds = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        //set the buttons
        buyGoldButton = findViewById(R.id.buy1)
        buyGoldXlButton = findViewById(R.id.buy2)
        buyCarButton = findViewById(R.id.buy3)
        buyBungalowButton = findViewById(R.id.buy4)

        //disable the buttons and do not enable until checking user balance
        buyBungalowButton?.isEnabled = false
        buyGoldXlButton?.isEnabled = false
        buyCarButton?.isEnabled = false
        buyBungalowButton?.isEnabled = false

        val ref = availableFunds?.collection("wallets")
                ?.document(mAuth?.currentUser?.email!!)

        //get user's balance and enable the buttons
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

        //User wants to buy gold
        buyGoldButton?.setOnClickListener{
            Log.d(TAG,"User wants to buy gold")
            //check for enough funds
            if(balance!! >100){
                //change the balance accordingly
                balance = balance!! + 50
                toast("Congratulations you bought gold worth 150!")
            }
        }

        //User wants to buy a lot of gold
        buyGoldXlButton?.setOnClickListener {
            Log.d(TAG,"User wants to buy a lot of gold")
            //check for enough funds
            if(balance!! >500){
                //change the balance accordingly
                balance = balance!!+ 500
                toast("Congratulations you bought gold worth 1000!")
            }
        }

        //User wants to buy a car
        buyCarButton?.setOnClickListener {
            Log.d(TAG,"User wants to buy a car")
            //check for enough funds
            if(balance!! >1000){
                //change the balance accordingly
                balance = balance!!- 1000
                toast("Congratulations on your new car!")
            }
        }

        //User wants to buy a house
        buyBungalowButton?.setOnClickListener {
            Log.d(TAG,"User wants to buy a bungalow")
            //check for enough funds
            if(balance!! >5000){
                //change the balance accordingly
                balance = balance!!- 5000
                toast("Congratulations on your new bungalow!")
            }
        }
        }

    //update the previous balance with the new balance in the user data
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
