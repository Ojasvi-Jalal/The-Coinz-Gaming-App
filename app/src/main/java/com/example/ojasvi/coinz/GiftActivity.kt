package com.example.ojasvi.coinz

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.jetbrains.anko.toast

//sends all the spare coins to the selected friend
class GiftActivity : Activity() {

    //friend's email
    private var sendeeEmail: EditText? = null
    //send button
    private var button: Button? = null

    //firebase and firestore objects to access the database
    private var mAuth: FirebaseAuth? = null
    private var wallet: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gift)

        //set the field to the relevant view
        sendeeEmail = findViewById(R.id.sendee)
        //set the send button
        button = findViewById(R.id.sendMoney)

        //get the firestore and firebase instances
        wallet = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()

        //enable send button
        button?.isEnabled = true


        val refSender = wallet?.collection("wallets")
                ?.document(mAuth?.currentUser?.email!!)

        //sends all the coins left in the wallet to the selected friend
        button!!.setOnClickListener {
            Log.d(TAG,"clicked on send")
            refSender?.collection("wallet")?.get()?.addOnCompleteListener { task ->
                val friendEmail = sendeeEmail!!.text.toString()
                //validates friend's email
                if(friendEmail != "") {
                    if (task.result != null) {
                        val refFriend = wallet?.collection("wallets")
                                ?.document(friendEmail)
                        Log.d(TAG, "sending over the coins")
                        //put the coins from the user's wallet to the friend's wallet
                        for (document in task.result!!) {
                            val coin = document.toObject(Coin::class.java)
                            document.reference.delete()
                            refFriend?.collection("wallet")?.add(coin)
                        }
                        toast("Successfully sent!")
                    }
                    //task unsuccessful: friend not present in the database?
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
