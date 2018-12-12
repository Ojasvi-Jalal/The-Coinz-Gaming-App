package com.example.ojasvi.coinz

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.ImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class WalletActivity : AppCompatActivity() {

    //For recycler view for the coins in the wallet
    private  lateinit var adapter: RecyclerAdapter
    private lateinit var  linearLayoutManager: LinearLayoutManager
    private lateinit var recyclerView: RecyclerView

    //list of the collected coins
    private var coins = ArrayList<Coin>()
    //list of the coins that need displayed
    private val displayCoins = ArrayList<Coin>()

    //shows all the shils when clicked on
    private var shilButton: ImageView? = null
    //shows all the dollars when clicked on
    private var dolrButton: ImageView? = null
    //shows all the quids when clicked on
    private var quidButton: ImageView? = null
    //shows all the penys when clicked on
    private var penyButton: ImageView? = null
    //shows all the different currencies when clicked on
    private var walletButton: ImageView? = null

    //opens the gift activity
    private var giftButton: ImageView? = null
    private var location:String? = ""

    //the firestore and the firebase objects
    private var wallet: FirebaseFirestore? = null
    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_wallet)

        //get the firestore and firebase instances
        wallet = FirebaseFirestore.getInstance()
        mAuth = FirebaseAuth.getInstance()


        //set the buttons
        walletButton = findViewById(R.id.wallet)
        shilButton = findViewById(R.id.shilWallet)
        dolrButton = findViewById(R.id.dolrWallet)
        quidButton = findViewById(R.id.quidWallet)
        penyButton = findViewById(R.id.penyWallet)
        giftButton = findViewById(R.id.gift)

        //get all the coins in the wallet
        val ref = wallet?.collection(WalletActivity.COLLECTION_KEY)?.document(mAuth?.currentUser?.email!!)

        //if any coin deposited update the wallet
        ref?.collection("wallet")?.addSnapshotListener{ snapshot, e ->
            if (e != null) {
                Log.w(TAG, "Listen failed.", e)
            }

            //update the list that the user is viewing instantly
            coins = ArrayList()
            for (document in snapshot!!)
                    coins.add(document.toObject(Coin::class.java))
            Log.d(TAG, "Wallet modified")

            if(location == "wallet"){
                displayWallet()
            }

            if(location == "peny"){
                displayPenys()
            }

            if(location == "dollar"){
                displayDolrs()
            }

            if(location == "quid"){
                displayQuids()
            }

            if(location == "shil"){
                displayShils()
            }
        }

        //get the recycler view ready
        recyclerView = findViewById(R.id.listOfCoins)
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager


        //show all the coins in the wallet
        walletButton!!.setOnClickListener{
            displayCoins.clear()
            Log.d(TAG,"Displaying all coins")
            displayWallet()
        }
        //filter the wallet and show the relevant coins

        shilButton!!.setOnClickListener {
            Log.d(TAG, "Displaying shils")
            displayShils()
        }

        dolrButton!!.setOnClickListener {
            Log.d(TAG, "Displaying dolrs")
            displayDolrs()
        }

        quidButton!!.setOnClickListener {
            Log.d(TAG, "Displaying quids")
            displayQuids()
        }

        penyButton!!.setOnClickListener {
            Log.d(TAG, "Displaying penys")
            displayPenys()
        }

        //open the gift activity
        giftButton!!.setOnClickListener{
            Log.d(TAG,"User wants to gift the coins")
            intent = Intent(this,GiftActivity::class.java)
            startActivity(intent)
        }

    }

    //display the penys
    private fun displayPenys(){
        displayCoins.clear()
        for(coin in coins)
            if (coin.currency == "PENY")
                displayCoins.add(coin)
        adapter = RecyclerAdapter(displayCoins,this)
        recyclerView.adapter = adapter
        location = "peny"
    }

    //display the dollars
    private fun displayDolrs(){
        displayCoins.clear()
        for(coin in coins)
            if (coin.currency == "DOLR")
                displayCoins.add(coin)
        adapter = RecyclerAdapter(displayCoins, this)
        recyclerView.adapter = adapter
        location = "dollar"
    }

    //display the shillings
    private fun displayShils(){
        displayCoins.clear()
        for(coin in coins)
            if (coin.currency == "SHIL")
                displayCoins.add(coin)
        adapter = RecyclerAdapter(displayCoins, this)
        recyclerView.adapter = adapter
        location = "shil"
    }

    //display the quids
    private fun displayQuids(){
        displayCoins.clear()
        for(coin in coins)
            if (coin.currency == "QUID")
                displayCoins.add(coin)
        adapter = RecyclerAdapter(displayCoins, this)
        recyclerView.adapter = adapter
        location = "quid"
    }

    //display every coin
    private fun displayWallet(){
        displayCoins.clear()
        displayCoins.addAll(coins)
        adapter = RecyclerAdapter(displayCoins, this)
        recyclerView.adapter = adapter
        location = "wallet"
    }

    companion object {
        private const val TAG = "WalletActivity"
        private const val COLLECTION_KEY = "wallets"
    }
}
