package com.example.ojasvi.coinz

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.toast

class GiftActivity : Activity() {

    private var sendeeEmail: EditText? = null
    private var button: Button? = null

    private var mAuth: FirebaseAuth? = null
    private var wallet: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gift)

        sendeeEmail = findViewById(R.id.sendee)
        button = findViewById(R.id.sendMoney)

        wallet = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        button?.isEnabled = true //todo: the right implementation


        val refSender = wallet?.collection("wallets")
                ?.document(mAuth?.currentUser?.email!!)

        button!!.setOnClickListener {
            Log.d(TAG,"clicked on send")
            refSender?.collection("wallet")?.get()?.addOnCompleteListener { task ->
                val friendEmail = sendeeEmail!!.text.toString()
                if(friendEmail != "") {
                    if (task.result != null) {
                        val refFriend = wallet?.collection("wallets")
                                ?.document(friendEmail)
                        Log.d(TAG, "sending over the coins")
                        for (document in task.result!!) {
                            val coin = document.toObject(Coin::class.java)
                            document.reference.delete()
                            refFriend?.collection("wallet")?.add(coin)
                        }
                        toast("Successfully sent!")
                    }
                    else {
                        Log.d(TAG, "mission unsuccessful!")
                    }
                }
                else {
                    Log.d(TAG, "email address not entered!: $friendEmail")
                }
            }
        }
    }

    companion object{
        const val TAG = "GiftActivity"
    }

}
