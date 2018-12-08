package com.example.ojasvi.coinz

import android.annotation.SuppressLint
import android.os.Bundle
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import kotlinx.android.synthetic.main.activity_bank.*
import java.math.BigDecimal

class BankActivity : Activity() {

    private var balance: TextView? = null
    private var walletButton: ImageView? = null
    private var shopButton: ImageView? = null
    private var totalBalance = BigDecimal(0)
    private var bankAccount: FirebaseFirestore? = null
    private var mAuth: FirebaseAuth? = null

    private val preferencesFile = "MyPrefsFile" // for getting preferences

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bank)

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

        val ref = bankAccount?.collection("wallets")
                ?.document(mAuth?.currentUser?.email!!)

        ref?.collection("User info")
                ?.document("Available funds")
                ?.addSnapshotListener { snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
            }

            balance = findViewById(R.id.userBalance)
            balance?.text = "%.3f".format(snapshot?.getDouble("Account Balance"))

        }

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
                                            var coin = document.toObject(Coin::class.java)
                                            //Log.d(TAG, coin.value.toString())
                                            if (shilRate != null) {
                                                if (coin.currency == "SHIL")
                                                    totalBalance += shilRate * coin.value?.toBigDecimal()
                                                Log.d(TAG, totalBalance.toString())
                                            }
                                            if (penyRate != null) {
                                                if (document.toObject(Coin::class.java).currency == "PENY")
                                                    totalBalance += penyRate * coin.value?.toBigDecimal()
                                                Log.d(TAG, totalBalance.toString())
                                            }
                                            if (dolrRate != null) {
                                                if (document.toObject(Coin::class.java).currency == "DOLR")
                                                    totalBalance += dolrRate * coin.value?.toBigDecimal()
                                                Log.d(TAG, totalBalance.toString())
                                            }
                                            if (quidRate != null) {
                                                if (document.toObject(Coin::class.java).currency == "QUID")
                                                    totalBalance += quidRate * coin.value?.toBigDecimal()
                                                Log.d(TAG, totalBalance.toString())
                                            }
                                        }
                                    setBalance()
                                }
                    }

                }

        walletButton = findViewById(R.id.wallet)
        shopButton = findViewById(R.id.goShopping)
        walletButton!!.isEnabled = true
        walletButton!!.setOnClickListener(){
            Log.d(TAG,"Openining wallet")
            intent = Intent(this,WalletActivity::class.java)
            startActivity(intent)
        }

        shopButton!!.setOnClickListener(){
            Log.d(TAG,"Going to the shop")
            intent = Intent(this,ShoppingActivity::class.java)
            startActivity(intent)
        }

    }

    fun setBalance(){
        balance = findViewById(R.id.userBalance)
        balance?.text = "%.3f".format(totalBalance)
        val netWorth = HashMap<String, Any?>()
        netWorth["Account Balance"] = totalBalance.toLong()
        var db = bankAccount?.collection("wallets")
                ?.document(mAuth?.currentUser?.email!!)
                ?.collection("User info")
        db
                ?.document("Available funds")
                ?.set(netWorth)
                ?.addOnSuccessListener { Log.d(TAG, "Account Balance successfully written!") }
                ?.addOnFailureListener { e -> Log.w(TAG, "Error writing document", e)}


        var nickname:String = ""
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
