package com.example.ojasvi.coinz

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.math.BigDecimal

class BankActivity : Activity() {

    //user's balance
    private var balance: TextView? = null
    //leads to the wallet if clicked
    private var walletButton: LinearLayout? = null
    //opens the shop if clicked
    private var shopButton: LinearLayout? = null
    //calculates the total balance
    private var totalBalance = BigDecimal(0)

    //initialise firestore and firebase objects
    private var bankAccount: FirebaseFirestore? = null
    private var mAuth: FirebaseAuth? = null

    // for getting preferences
    private val preferencesFile = "MyPrefsFile"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank)

        //get the firestore and firebase instances
        bankAccount = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()



        //Restore preferences and get the rates to do conversion
        val prefSettings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        val shilRate = prefSettings?.getString("shilRate","0")?.toBigDecimal()
        Log.d(TAG,shilRate.toString())
        val dolrRate = prefSettings?.getString("dolrRate","0")?.toBigDecimal()
        Log.d(TAG,dolrRate.toString())
        val penyRate = prefSettings?.getString("penyRate","0")?.toBigDecimal()
        Log.d(TAG,penyRate.toString())
        val quidRate = prefSettings?.getString("quidRate","0")?.toBigDecimal()
        Log.d(TAG,quidRate.toString())

        //to access firebase for the data
        val ref = bankAccount?.collection("wallets")
                ?.document(mAuth?.currentUser?.email!!)

        //if a value already present in the document @Available funds" display that
        //everytime after first time
        ref?.collection("User info")
                ?.document("Available funds")
                ?.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
            }

            balance = findViewById(R.id.userBalance)
            balance?.text = "%.3f".format(snapshot?.getDouble("Account Balance"))

        }

        //otherwise calculate the total available balance
        ref?.collection("User info")
            ?.document("Available funds")
            ?.get()
            ?.addOnSuccessListener {
                if(it.exists() && it.data!!["Account Balance"] != null)
                {
                    totalBalance = (it.data!!["Account Balance"] as Long).toBigDecimal()
                    setBalance()
                }
                else {
                    ref.collection("account")
                        .get()
                        .addOnCompleteListener { task ->
                        if (task.result != null)
                            for (document in task.result!!) {
                                val coin = document.toObject(Coin::class.java)
                                //convert every currency to gold
                                if (shilRate != null) {
                                    if (coin.currency == "SHIL")
                                        totalBalance += shilRate * coin.value.toBigDecimal()
                                    Log.d(TAG, totalBalance.toString())
                                }
                                if (penyRate != null) {
                                    if (document.toObject(Coin::class.java).currency == "PENY")
                                        totalBalance += penyRate * coin.value.toBigDecimal()
                                    Log.d(TAG, totalBalance.toString())
                                }
                                if (dolrRate != null) {
                                    if (document.toObject(Coin::class.java).currency == "DOLR")
                                        totalBalance += dolrRate * coin.value.toBigDecimal()
                                    Log.d(TAG, totalBalance.toString())
                                }
                                if (quidRate != null) {
                                    if (document.toObject(Coin::class.java).currency == "QUID")
                                        totalBalance += quidRate * coin.value.toBigDecimal()
                                    Log.d(TAG, totalBalance.toString())
                                }
                            }
                            setBalance()
                        }
                    }

                }

        //set the buttons according to the relevant views in the relevant layout
        walletButton = findViewById(R.id.wallet)
        shopButton = findViewById(R.id.goShopping)

        //make the wallet button clickable
        walletButton!!.isEnabled = true

        //open the wallet activity
        walletButton!!.setOnClickListener {
            Log.d(TAG,"Openining wallet")
            intent = Intent(this,WalletActivity::class.java)
            startActivity(intent)
        }

        //open the shop activity
        shopButton!!.setOnClickListener {
            Log.d(TAG,"Going to the shop")
            intent = Intent(this,ShoppingActivity::class.java)
            startActivity(intent)
        }

    }


    private fun setBalance(){
        //displays the acoount balance on the bank activity view
        balance = findViewById(R.id.userBalance)
        balance?.text = "%.3f".format(totalBalance)
        val netWorth = HashMap<String, Any?>()
        netWorth["Account Balance"] = totalBalance.toLong()

        //writes the account balance into "Available funds" document on firestore
        val db = bankAccount?.collection("wallets")
                ?.document(mAuth?.currentUser?.email!!)
                ?.collection("User info")
        db
                ?.document("Available funds")
                ?.set(netWorth)
                ?.addOnSuccessListener { Log.d(TAG, "Account Balance successfully written!") }
                ?.addOnFailureListener { e -> Log.w(TAG, "Error writing document", e)}

        //store user's nickname and total balance in "scores" document for the leaderboard
        var nickname: String
        bankAccount?.runTransaction { transaction ->
            if(db != null) {
                val doc = transaction.get(db.document("Nickname"))
                if(doc.exists()) {
                    nickname = doc.getString("Nickname")!!
                    val score = HashMap<String, Any?>()
                    score[nickname] = totalBalance.toLong()
                    transaction.set(bankAccount?.collection("wallets")?.document("scores")!!,score)
                }
            }
        }


    }

    companion object {
        var TAG = "Bank"
    }

}
